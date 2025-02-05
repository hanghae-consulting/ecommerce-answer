package com.example.redis.controllers;

import com.example.redis.services.RedisListsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TasksController {
    @Autowired
    private RedisListsService redisListsService;

    @PostMapping("/{queue}")
    public String addTask(@PathVariable String queue, @RequestParam String task) {
        redisListsService.addTask(queue, task);
        return "Task added to queue: " + task;
    }

    @GetMapping("/{queue}")
    public String getTask(@PathVariable String queue){
        String task = redisListsService.getTask(queue);
        return task != null ? "Task processed: " + task : "No tasks in queue.";
    }

    @GetMapping("/{queue}/all")
    public List<String> getAllTasks(@PathVariable String queue){
        return redisListsService.getAllTasks(queue);
    }
}
