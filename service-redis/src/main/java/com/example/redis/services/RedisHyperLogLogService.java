package com.example.redis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisHyperLogLogService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addElement(String key, String value) {
        redisTemplate.opsForHyperLogLog().add(key, value);
    }

    public Long getCount(String key) {
        return redisTemplate.opsForHyperLogLog().size(key);
    }
}