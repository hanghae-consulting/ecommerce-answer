package com.example.recommand.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RecommendSortedSetsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 상품 인기도 갱신
    public void updatePopularity(String leaderboard, String itemId, int score) {
        redisTemplate.opsForZSet().incrementScore(leaderboard, itemId, score);
    }

    // 인기 상품 조회
    public Set<String> getTopItems(String leaderboard, int count) {
        return redisTemplate.opsForZSet().reverseRange(leaderboard, 0, count - 1);
    }
}
