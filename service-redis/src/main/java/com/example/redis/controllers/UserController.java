package com.example.redis.controllers;

import com.example.redis.dto.SaveUserDto;
import com.example.redis.services.RedisSetsService;
import com.example.redis.services.RedisStringsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private RedisStringsService redisStringsService;

    @Autowired
    private RedisSetsService redisSetsService;

    @PostMapping("/{id}")
    public String saveUser(@PathVariable String id, @RequestBody SaveUserDto saveUserDto) {
        redisStringsService.saveUserProfile(id, saveUserDto.getName(), saveUserDto.getEmail());
        return "User saved successfully!";
    }

    @GetMapping("/{id}")
    public String getUser(@PathVariable String id) {
        return redisStringsService.getUserProfile(id);
    }

    @PostMapping("/set/{set}")
    public String addUser(@PathVariable String set, @RequestParam String user) {
        redisSetsService.addUser(set, user);
        return "User added: " + user;
    }

    @GetMapping("/set/{set}")
    public Set<String> getAllUsers(@PathVariable String set) {
        return redisSetsService.getAllUsers(set);
    }

    @DeleteMapping("/set/{set}")
    public String removeUser(@PathVariable String set, @RequestParam String user) {
        redisSetsService.removeUser(set, user);
        return "User removed: " + user;
    }
}
