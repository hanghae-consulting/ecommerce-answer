package com.example.redis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RedisSortedSetsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addScore(String leaderboard, String player, double score) {
        redisTemplate.opsForZSet().add(leaderboard, player, score);
    }

    public Set<ZSetOperations.TypedTuple<String>> getTopPlayers(String leaderboard, int topN) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(leaderboard, 0, topN - 1);
    }
}
