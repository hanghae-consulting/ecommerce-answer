package com.example.recommand.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RecommendStreamsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 이벤트 추가
    public void addEvent(String streamName, String userId, String action, String itemId) {
        Map<String, String> event = Map.of(
                "userId", userId,
                "action", action,
                "itemId", itemId,
                "timestamp", String.valueOf(System.currentTimeMillis())
        );
        redisTemplate.opsForStream().add(streamName, event);
    }

    // 이벤트 조회
    public List<MapRecord<String, Object, Object>> getEvents(String streamName) {
        return redisTemplate.opsForStream().range(streamName, Range.unbounded());
    }
}
