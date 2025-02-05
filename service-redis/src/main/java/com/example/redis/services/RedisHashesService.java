package com.example.redis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RedisHashesService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void saveUser(String userId, Map<String, String> userInfo){
        redisTemplate.opsForHash().putAll("user:" + userId, userInfo);
    }

    public Map<Object, Object> getUser(String userId){
        return redisTemplate.opsForHash().entries("user:" + userId);
    }

    public void updateUserField(String userId, String field, String value){
        redisTemplate.opsForHash().put("user:" + userId , field, value);
    }
}
