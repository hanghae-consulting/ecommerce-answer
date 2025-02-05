package com.example.redis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class RedisStreamsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String addStreamEntry(String streamName, String sensor, String value) {
        Map<String, String> data = Map.of("sensor", sensor, "value", value);
        RecordId recordId = redisTemplate.opsForStream().add(streamName, data);
        return Objects.requireNonNull(recordId).getValue();
    }

    public List<MapRecord<String, Object, Object>> getStreamEntries(String streamName, String start, String end) {
        Range<String> range = Range.closed(start, end);
        return redisTemplate.opsForStream().range(streamName, range);
    }
}
