package com.example.redis.controllers;

import com.example.redis.services.RedisBitmapsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logins")
public class LoginController {
    @Autowired
    private RedisBitmapsService redisBitmapsService;

    @PostMapping("/{date}/{userId}")
    public String loginUser(@PathVariable String date, @PathVariable int userId) {
        redisBitmapsService.setUserLogin(date, userId);
        return "User " + userId + " logged on " + date;
    }

    @GetMapping("/{date}/{userId}")
    public String checkLogin(@PathVariable String date, @PathVariable int userId) {
        boolean loggedIn = redisBitmapsService.isUserLoggedIn(date, userId);
        return "User " + userId + " login status on " +date;
    }

    @GetMapping("{date}/total")
    public String getTotalLogins(@PathVariable String date) {
        long totalLogins = redisBitmapsService.countBits("login:" + date);
        return "Total logins on " + date + ": " + totalLogins;
    }
}
