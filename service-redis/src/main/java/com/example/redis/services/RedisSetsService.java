package com.example.redis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RedisSetsService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addUser(String setName, String user) {
        redisTemplate.opsForSet().add(setName, user);
    }

    public Set<String> getAllUsers(String setName){
        return redisTemplate.opsForSet().members(setName);
    }

    public void removeUser(String setName, String user){
        redisTemplate.opsForSet().remove(setName, user);
    }
}
