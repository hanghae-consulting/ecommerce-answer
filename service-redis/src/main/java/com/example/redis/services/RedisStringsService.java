package com.example.redis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisStringsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void saveUserProfile(String userId, String name, String email) {
        redisTemplate.opsForValue().set("user:" + userId + ":name", name);
        redisTemplate.opsForValue().set("user:" + userId + ":email", name);
    }

    public String getUserProfile(String userId) {
        String name = redisTemplate.opsForValue().get("user:" + userId + ":name");
        String email = redisTemplate.opsForValue().get("user:" + userId + ":email");
        return "Name: " + name + ", Email: " + email;
    }
}
