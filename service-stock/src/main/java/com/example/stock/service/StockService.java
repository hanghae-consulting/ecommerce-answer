package com.example.stock.service;

import com.example.annotations.RedissonLock;
import com.example.cache.CachePublisher;
import com.example.kafka.CreateStockEvent;
import com.example.kafka.DecreaseStockEvent;
import com.example.kafka.UpdateStockEvent;
import com.example.stock.entity.Stock;
import com.example.stock.repository.StockRepository;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private static final String STOCK_KEY_PREFIX = "stock:";

    private final StockRepository stockRepository;

    // ───────────── 추가된 부분 ─────────────
    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CachePublisher cachePublisher;
    // ────────────────────────────────────


    /**
     * 재고(Stock) 생성
     *  1. DB에 저장
     *  2. 캐시에 저장
     */
    public Stock createStock(CreateStockEvent event) {
        Stock stock = Stock.builder()
                .stockId(event.getStockId())
                .storeId(event.getStoreId())
                .productId(event.getProductId())
                .stock(event.getStock())
                .build();

        Stock savedStock = stockRepository.saveAndFlush(stock);

        // 캐시 키
        String cacheKey = STOCK_KEY_PREFIX + savedStock.getStockId();

        // Redis 캐시에 저장 (예: 1시간)
        redisTemplate.opsForValue().set(cacheKey, savedStock);
        // 로컬 캐시에 저장
        localCache.put(cacheKey, savedStock);

        log.info("Created stock: {}", cacheKey);
        return savedStock;
    }


    /**
     * 재고 조회
     *  - 로컬 캐시 → Redis → DB 순서
     */
    public Stock getStock(String stockId) {
        String cacheKey = STOCK_KEY_PREFIX + stockId;

        // 1) 로컬 캐시 확인
        Stock cachedStock = (Stock) localCache.getIfPresent(cacheKey);
        if (cachedStock != null) {
            log.info("[LocalCache] Hit for key={}", cacheKey);
            return cachedStock;
        }

        // 2) Redis 캐시 확인
        cachedStock = (Stock) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStock != null) {
            log.info("[RedisCache] Hit for key={}", cacheKey);
            // 로컬 캐시에 다시 저장
            localCache.put(cacheKey, cachedStock);
            return cachedStock;
        }

        // 3) DB 조회
        Stock dbStock = stockRepository.findByStockId(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));

        // 캐시에 저장
        redisTemplate.opsForValue().set(cacheKey, dbStock);
        localCache.put(cacheKey, dbStock);

        return dbStock;
    }


    /**
     * 재고 수정
     *  - DB 수정
     *  - 캐시 갱신
     *  - Pub/Sub 메시지 발행 (다른 서버 캐시 무효화)
     */
    public Stock updateStock(UpdateStockEvent event) {
        String stockId = event.getStockId();
        Stock stock = stockRepository.findByStockId(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));

        stock.setStoreId(event.getStoreId());
        stock.setProductId(event.getProductId());
        stock.setStock(event.getStock());

        Stock savedStock = stockRepository.save(stock);

        // 캐시 키
        String cacheKey = STOCK_KEY_PREFIX + savedStock.getStockId();

        // Redis, 로컬 캐시에 갱신
        redisTemplate.opsForValue().set(cacheKey, savedStock);
        localCache.put(cacheKey, savedStock);

        // 다른 서버 인스턴스 캐시 무효화
        String message = "Updated stock-" + cacheKey;
        cachePublisher.publish("cache-sync", message);

        log.info("Updated stock: {}, published message: {}", cacheKey, message);
        return savedStock;
    }


    /**
     * 재고 감소
     *  - 동시성 제어(@RedissonLock) + DB 수정
     *  - 캐시 갱신
     *  - Pub/Sub 메시지 발행
     */
    @RedissonLock(value="#stock-3f29c8e4-7b1a-4d5f-9c3e-8a2b6d4e7f10")
    public Stock decreaseStock(DecreaseStockEvent event) {
        String stockId = event.getStockId();
        Stock stock = stockRepository.findByStockId(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));

        if (!stock.decrease(event.getQuantity())) {
            throw new RuntimeException("The quantity is larger than the stock: " + stockId);
        }

        Stock savedStock = stockRepository.save(stock);

        // 캐시 갱신
        String cacheKey = STOCK_KEY_PREFIX + savedStock.getStockId();
        redisTemplate.opsForValue().set(cacheKey, savedStock);
        localCache.put(cacheKey, savedStock);

        // 다른 서버에 무효화 메시지
        String message = "Updated stock-" + cacheKey;
        cachePublisher.publish("cache-sync", message);

        log.info("DecreaseStock, updated stock: {}, published: {}", cacheKey, message);
        return savedStock;
    }


    /**
     * 재고 삭제
     *  - DB에서 삭제
     *  - 캐시 무효화
     *  - Pub/Sub 메시지 발행
     */
    public void deleteStock(String stockId) {
        Stock stock = stockRepository.findByStockId(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));
        stockRepository.delete(stock);

        String cacheKey = STOCK_KEY_PREFIX + stockId;

        // 현재 서버 캐시 무효화
        localCache.invalidate(cacheKey);
        redisTemplate.delete(cacheKey);

        // 다른 서버도 무효화하도록 메시지 발행
        String message = "Deleted stock-" + cacheKey;
        cachePublisher.publish("cache-sync", message);

        log.info("Deleted stock: {}, published message: {}", cacheKey, message);
    }
}
