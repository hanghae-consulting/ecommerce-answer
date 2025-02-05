package com.example.product.service;

import com.example.kafka.CreateProductEvent;
import com.example.kafka.UpdateProductEvent;
import com.example.cache.CachePublisher;
import com.example.product.dto.ProductMetricsDto;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ───────────── 추가된 부분 ─────────────
    // 로컬 캐시(Caffeine)
    private final Cache<String, Object> localCache;

    // RedisTemplate
    private final RedisTemplate<String, Object> redisTemplate;

    // Pub/Sub으로 캐시 무효화 메시지를 발행하는 컴포넌트
    private final CachePublisher cachePublisher;
    // ────────────────────────────────────

    /**
     * 상품 생성
     *  - DB에 저장
     *  - 캐시에 저장
     */
    public Product createProduct(CreateProductEvent event) {
        Product product = Product.builder()
                .productId(event.getProductId())
                .name(event.getName())
                .price(event.getPrice())
                .build();

        Product savedProduct = productRepository.saveAndFlush(product);

        // 캐시 키
        String cacheKey = "product:" + savedProduct.getProductId();

        // Redis에 저장 (TTL 예: 1시간)
        redisTemplate.opsForValue().set(cacheKey, savedProduct, 1, TimeUnit.HOURS);

        // 로컬 캐시에 저장
        localCache.put(cacheKey, savedProduct);

        log.info("Created product: {}", cacheKey);
        return savedProduct;
    }

    /**
     * 상품 조회
     *  - 로컬 캐시 → Redis → DB 순으로 조회
     */
    public Product getProduct(String productId) {
        String cacheKey = "product:" + productId;

        // 1) 로컬 캐시 확인
        Product cached = (Product) localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.info("[LocalCache] Hit for key={}", cacheKey);
            return cached;
        }

        // 2) Redis 캐시 확인
        cached = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("[RedisCache] Hit for key={}", cacheKey);
            // 로컬 캐시에 다시 저장
            localCache.put(cacheKey, cached);
            return cached;
        }

        // 3) DB 조회
        Product dbProduct = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // 조회 후 캐시에 저장
        redisTemplate.opsForValue().set(cacheKey, dbProduct, 1, TimeUnit.HOURS);
        localCache.put(cacheKey, dbProduct);

        return dbProduct;
    }

    /**
     * 상품 수정
     *  - DB 수정
     *  - 캐시 갱신
     *  - Pub/Sub 메시지 발행하여 다른 서버 캐시 무효화
     */
    public Product updateProduct(UpdateProductEvent event) {
        String productId = event.getProductId();
        log.info("Update product by id: {}", productId);

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        product.setName(event.getName());
        product.setPrice(event.getPrice());

        Product savedProduct = productRepository.save(product);

        // 캐시 키
        String cacheKey = "product:" + savedProduct.getProductId();

        // Redis, 로컬 캐시에 갱신
        redisTemplate.opsForValue().set(cacheKey, savedProduct);
        localCache.put(cacheKey, savedProduct);

        // 다른 서버들도 캐시 무효화(혹은 갱신)하도록 메시지 발행
        // 예: "Updated product-product:xxx"
        String message = "Updated product-" + cacheKey;
        cachePublisher.publish("cache-sync", message);

        log.info("Updated product: {}, published message: {}", cacheKey, message);
        return savedProduct;
    }

    /**
     * 상품 삭제
     *  - DB 삭제
     *  - 캐시 무효화
     *  - Pub/Sub 메시지 발행 (다른 서버 캐시 무효화)
     */
    public void deleteProduct(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        productRepository.delete(product);

        String cacheKey = "product:" + productId;
        // 즉시 캐시 무효화 (현재 서버)
        localCache.invalidate(cacheKey);
        redisTemplate.delete(cacheKey);

        // 다른 서버 무효화 메시지 발행
        // 예: "Deleted product-product:xxx"
        String message = "Deleted product-" + cacheKey;
        cachePublisher.publish("cache-sync", message);

        log.info("Deleted product: {}, published message: {}", cacheKey, message);
    }

    // ─────────────────────────────────────────────────────────
    // 아래는 "메트릭" 관련 로직 (Redis를 이용한 좋아요, 방문수, 검색 기록 등)
    // 기존 코드와 동일하되 필요하다면 캐시 키 충돌에 주의해주세요.
    // ─────────────────────────────────────────────────────────

    /**
     * 상품에 좋아요를 추가
     */
    public void likeProduct(String productId, String username) {
        String key = "product:likes:" + productId;
        // Set<String>을 사용하여 중복 좋아요 방지
        redisTemplate.opsForSet().add(key, username);
    }

    /**
     * 상품 방문 기록 (방문 수 증가)
     */
    public void visitProduct(String productId) {
        String key = "product:visits:" + productId;
        long currentCount = 0L;

        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Long) {
            currentCount = (Long) value;
        } else if (value instanceof Integer) {
            currentCount = ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                currentCount = Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.error("Invalid visits count format for key {}: {}", key, e.getMessage());
            }
        }

        // 방문 수 증가
        currentCount += 1;

        // 증가된 방문 수 저장 (1일 후 만료)
        redisTemplate.opsForValue().set(key, currentCount, 1, TimeUnit.DAYS);
    }

    /**
     * 상품 검색 기록 저장 (검색어 리스트)
     */
    public void searchProduct(String productId, String query) {
        String key = "user:searches:" + productId;
        redisTemplate.opsForList().rightPush(key, query);
        // 최근 10개로 제한
        redisTemplate.opsForList().trim(key, -10, -1);
    }

    /**
     * 좋아요 수 조회
     */
    public Long getLikesCount(String productId) {
        String key = "product:likes:" + productId;
        Long count = redisTemplate.opsForSet().size(key);
        return (count != null) ? count : 0;
    }

    /**
     * 방문자 수 조회
     */
    public Long getVisitsCount(String productId) {
        String key = "product:visits:" + productId;
        Object value = redisTemplate.opsForValue().get(key);

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.error("Invalid visits count format for key {}: {}", key, e.getMessage());
            }
        } else {
            log.warn("Unexpected type for key {}: {}", key, value != null ? value.getClass().getName() : "null");
        }
        return 0L;
    }

    /**
     * 최근 검색 기록 조회
     */
    public List<String> getRecentSearches(String productId) {
        String key = "user:searches:" + productId;
        List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
        if (rawList == null) {
            return List.of();
        }
        return rawList.stream().map(Object::toString).toList();
    }

    /**
     * 상품 메트릭 DTO 조회
     */
    public ProductMetricsDto getProductMetrics(String productId) {
        Long likesCount = getLikesCount(productId);
        Long visitsCount = getVisitsCount(productId);
        List<String> recentSearches = getRecentSearches(productId);
        return new ProductMetricsDto(productId, likesCount, visitsCount, recentSearches);
    }

    public long getProductCount() {
        return productRepository.count();
    }
}
