package com.example.redis.controllers;

import com.example.redis.services.RedisHyperLogLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hyperloglog")
public class HyperLogLogController {

    @Autowired
    private RedisHyperLogLogService redisHyperLogLogService;

    @PostMapping("/{key}")
    public String addElement(@PathVariable String key, @RequestParam String value) {
        redisHyperLogLogService.addElement(key, value);
        return "Element added: " + value;
    }

    @GetMapping("/{key}/count")
    public String getCount(@PathVariable String key) {
        return "Approximate count of unique elements: " + redisHyperLogLogService.getCount(key);
    }
}
