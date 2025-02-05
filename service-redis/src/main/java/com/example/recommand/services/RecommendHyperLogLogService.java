package com.example.recommand.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RecommendHyperLogLogService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 고유 사용자 추가
    public void addUser(String hyperLogLogKey, String userId) {
        redisTemplate.opsForHyperLogLog().add(hyperLogLogKey, userId);
    }

    // 고유 사용자 수 조회
    public long getUniqueUsers(String hyperLogLogKey) {
        return redisTemplate.opsForHyperLogLog().size(hyperLogLogKey);
    }
}
