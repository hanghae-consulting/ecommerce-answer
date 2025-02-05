package com.example.redis.controllers;

import com.example.redis.services.RedisStreamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/streams")
public class StreamsController {

    @Autowired
    RedisStreamsService redisStreamsService;

    @PostMapping("/{streamName}")
    public String addStreamEntry(@PathVariable String streamName, @RequestParam String sensor, @RequestParam String value) {
        return "Record added with ID: " + redisStreamsService.addStreamEntry(streamName, sensor, value);
    }

    @GetMapping("/{streamName}")
    public List<MapRecord<String, Object, Object>> getStreamName(@PathVariable String streamName, @RequestParam String start, @RequestParam String end) {
        return redisStreamsService.getStreamEntries(streamName, start, end);
    }
}
