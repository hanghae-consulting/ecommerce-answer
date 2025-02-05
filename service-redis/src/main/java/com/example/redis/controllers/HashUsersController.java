package com.example.redis.controllers;

import com.example.redis.services.RedisHashesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hashusers")
public class HashUsersController {

    @Autowired
    private RedisHashesService redisHashesService;

    @PostMapping("/{id}")
    public String saveUser(@PathVariable("id") String id, @RequestBody Map<String, String> userInfo) {
        redisHashesService.saveUser(id, userInfo);
        return "User info saved: " + id;
    }

    @GetMapping("/{id}")
    public Map<Object, Object> getUser(@PathVariable("id") String id) {
        return redisHashesService.getUser(id);
    }

    @PutMapping("/{id}")
    public String updateUserField(@PathVariable String id, @RequestParam String field, @RequestParam String value){
        redisHashesService.updateUserField(id, field, value);
        return "Field updated for user: " + id;
    }
}
