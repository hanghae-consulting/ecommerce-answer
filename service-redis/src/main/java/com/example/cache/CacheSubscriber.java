package com.example.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheSubscriber implements MessageListener {

    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        StringRedisSerializer serializer = new StringRedisSerializer();
        String body = serializer.deserialize(message.getBody());

        System.out.println("Received message: " + body);

        assert body != null;
        if (body.contains("Updated product-") || body.contains("Deleted product-")) {
            String cachedKey = body.split("-")[1];

            System.out.println(cachedKey);
            localCache.invalidate(cachedKey);
            redisTemplate.delete(cachedKey);
            System.out.println("Invalidated local cache for product: " + cachedKey);
        }
    }
}
