package com.example.store.service;

import com.example.cache.CachePublisher;
import com.example.kafka.CreateStoreEvent;
import com.example.kafka.UpdateStoreEvent;
import com.example.store.entity.Store;
import com.example.store.repository.StoreRepository;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private static final String STORE_KEY_PREFIX = "store:";

    private final StoreRepository storeRepository;

    // ───────────── 추가된 부분 ─────────────
    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CachePublisher cachePublisher;
    // ────────────────────────────────────

    /**
     * 매장(Store) 생성
     *  - DB 저장
     *  - 캐시에 저장
     */
    public Store createStore(CreateStoreEvent event) {
        Store store = Store.builder()
                .storeId(event.getStoreId())
                .storeName(event.getStoreName())
                .ownerName(event.getOwnerName())
                .address(event.getAddress())
                .phoneNumber(event.getPhoneNumber())
                .build();

        Store savedStore = storeRepository.saveAndFlush(store);

        // 캐시 키
        String cacheKey = STORE_KEY_PREFIX + savedStore.getStoreId();

        // Redis 캐시에 저장 (예: TTL 필요하면 set(..., 1, TimeUnit.HOURS))
        redisTemplate.opsForValue().set(cacheKey, savedStore);

        // 로컬 캐시에 저장
        localCache.put(cacheKey, savedStore);

        return savedStore;
    }

    /**
     * 매장(Store) 수정
     *  - DB 수정
     *  - 캐시 갱신
     *  - Pub/Sub 메시지 발행 (다른 서버 캐시 무효화)
     */
    public Store updateStore(UpdateStoreEvent event) {
        String storeId = event.getStoreId();
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));

        store.setStoreName(event.getStoreName());
        store.setOwnerName(event.getOwnerName());
        store.setAddress(event.getAddress());
        store.setPhoneNumber(event.getPhoneNumber());

        Store savedStore = storeRepository.save(store);

        // 캐시 키
        String cacheKey = STORE_KEY_PREFIX + savedStore.getStoreId();

        // Redis, 로컬 캐시에 갱신
        redisTemplate.opsForValue().set(cacheKey, savedStore);
        localCache.put(cacheKey, savedStore);

        // 다른 서버도 무효화하도록 메시지 발행
        String message = "Updated store-" + cacheKey;
        cachePublisher.publish("cache-sync", message);

        return savedStore;
    }

    /**
     * 매장(Store) 삭제
     *  - DB 삭제
     *  - 캐시 무효화
     *  - Pub/Sub 메시지 발행
     */
    public void deleteStore(String storeId) {
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));

        storeRepository.delete(store);

        String cacheKey = STORE_KEY_PREFIX + storeId;

        // 현재 서버 캐시 무효화
        localCache.invalidate(cacheKey);
        redisTemplate.delete(cacheKey);

        // 다른 서버에서도 무효화하도록 메시지 발행
        String message = "Deleted store-" + cacheKey;
        cachePublisher.publish("cache-sync", message);
    }

    /**
     * 매장(Store) 조회
     *  - 로컬 캐시 -> Redis -> DB 순
     */
    public Store getStore(String storeId) {
        String cacheKey = STORE_KEY_PREFIX + storeId;

        // 1) 로컬 캐시 확인
        Store cachedStore = (Store) localCache.getIfPresent(cacheKey);
        if (cachedStore != null) {
            return cachedStore;
        }

        // 2) Redis 확인
        cachedStore = (Store) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStore != null) {
            localCache.put(cacheKey, cachedStore);
            return cachedStore;
        }

        // 3) DB 조회
        Store dbStore = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));

        // 캐시에 저장
        redisTemplate.opsForValue().set(cacheKey, dbStore);
        localCache.put(cacheKey, dbStore);

        return dbStore;
    }
}
