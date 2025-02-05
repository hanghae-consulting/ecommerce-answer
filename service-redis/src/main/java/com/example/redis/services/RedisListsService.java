package com.example.redis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisListsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addTask(String queueName, String task){
        redisTemplate.opsForList().leftPush(queueName, task);
    }

    public String getTask(String queueName) {
        return redisTemplate.opsForList().rightPop(queueName);
    }

    public List<String> getAllTasks(String queueName) {
        return redisTemplate.opsForList().range(queueName, 0, -1);
    }
}
