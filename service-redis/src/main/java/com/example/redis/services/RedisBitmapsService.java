package com.example.redis.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisBitmapsService {
    private static final Logger log = LoggerFactory.getLogger(RedisBitmapsService.class);
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void setUserLogin(String date, int userId){
        redisTemplate.opsForValue().setBit("login:" + date, userId, true);
    }

    public boolean isUserLoggedIn(String date, int userId){
        return Boolean.TRUE.equals(redisTemplate.opsForValue().getBit("login:" + date, userId));
    }

    public Long countBits(String key){
        // BITCOUNT 명령 실행
        log.info(key);
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.stringCommands().bitCount(key.getBytes()));
    }
}
