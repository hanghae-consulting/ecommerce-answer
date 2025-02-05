package com.example.order.service;

import com.example.cache.CachePublisher;
import com.example.kafka.CreateOrderEvent;
import com.example.kafka.UpdateOrderEvent;
import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String ORDER_KEY_PREFIX = "order:";

    // 로컬 캐시 (Caffeine)
    private final Cache<String, Object> localCache;

    // RedisTemplate
    private final RedisTemplate<String, Object> redisTemplate;

    // 메시지 발행 (Pub/Sub) 컴포넌트
    private final CachePublisher cachePublisher;

    private final OrderRepository orderRepository;


    /**
     * 1) 주문 생성
     *    - DB 저장 -> Redis + 로컬 캐시에 저장
     */
    public Order createOrder(CreateOrderEvent event) {
        Order order = Order.builder()
                .orderId(event.getOrderId())
                .storeId(event.getStoreId())
                .productId(event.getProductId())
                .stockId(event.getStockId())
                .quantity(event.getQuantity())
                .build();

        Order savedOrder = orderRepository.saveAndFlush(order);

        // 캐시 키
        String cacheKey = ORDER_KEY_PREFIX + savedOrder.getOrderId();

        // Redis 캐시 저장 (예: 1시간 TTL)
        redisTemplate.opsForValue().set(cacheKey, savedOrder, 1, TimeUnit.HOURS);

        // 로컬 캐시 저장
        localCache.put(cacheKey, savedOrder);

        log.info("Created order: {}", cacheKey);
        return savedOrder;
    }


    /**
     * 2) 주문 조회
     *    - 로컬 캐시 -> Redis -> DB 순서로 조회
     *    - 없으면 DB에서 조회 후 캐시에 저장
     */
    public Order getOrder(String orderId) {
        String cacheKey = ORDER_KEY_PREFIX + orderId;

        // 1) 로컬 캐시 조회
        Order cachedOrder = (Order) localCache.getIfPresent(cacheKey);
        if (cachedOrder != null) {
            log.info("[LocalCache] Hit for key={}", cacheKey);
            return cachedOrder;
        }

        // 2) Redis 캐시 조회
        cachedOrder = (Order) redisTemplate.opsForValue().get(cacheKey);
        if (cachedOrder != null) {
            log.info("[RedisCache] Hit for key={}", cacheKey);
            // 로컬 캐시에 다시 저장
            localCache.put(cacheKey, cachedOrder);
            return cachedOrder;
        }

        // 3) DB 조회
        Order dbOrder = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // 캐시에 저장 (TTL 설정도 동일하게 적용할 수 있음)
        redisTemplate.opsForValue().set(cacheKey, dbOrder);
        localCache.put(cacheKey, dbOrder);

        return dbOrder;
    }


    /**
     * 3) 주문 수정
     *    - DB 저장 후 Redis + 로컬 캐시 갱신
     *    - 다른 서버 인스턴스들 캐시 무효화를 위해 메시지 발행
     */
    public Order updateOrder(UpdateOrderEvent event) {
        String orderId = event.getOrderId();
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStoreId(event.getStoreId());
        order.setProductId(event.getProductId());
        order.setQuantity(event.getQuantity());

        Order savedOrder = orderRepository.save(order);

        String cacheKey = ORDER_KEY_PREFIX + savedOrder.getOrderId();
        // Redis, 로컬 캐시에 갱신
        redisTemplate.opsForValue().set(cacheKey, savedOrder);
        localCache.put(cacheKey, savedOrder);

        // 다른 서버 인스턴스 캐시 무효화를 위해 메시지 발행
        // 메시지 형식: "Updated order-order:xxxx" 로 가정
        String message = "Updated order-" + cacheKey;
        cachePublisher.publish("cache-sync", message);

        log.info("Updated order: {}, published message: {}", cacheKey, message);

        return savedOrder;
    }


    /**
     * 4) 주문 삭제
     *    - DB에서 삭제
     *    - Redis + 로컬 캐시도 무효화
     *    - 다른 서버 인스턴스들도 캐시 무효화를 위해 메시지 발행
     */
    public void deleteOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        orderRepository.delete(order);

        // 캐시 무효화 대상 key
        String cacheKey = ORDER_KEY_PREFIX + orderId;

        // 현재 서버(로컬 캐시 + Redis)에서도 삭제
        localCache.invalidate(cacheKey);
        redisTemplate.delete(cacheKey);

        // 다른 서버들도 캐시를 무효화하도록 메시지 발행
        // 메시지 형식: "Deleted order-order:xxxx" 로 가정
        String message = "Deleted order-" + cacheKey;
        cachePublisher.publish("cache-sync", message);

        log.info("Deleted order: {}, published message: {}", cacheKey, message);
    }
}
