package com.example.redis.controllers;

import com.example.redis.dto.LeaderboardDto;
import com.example.redis.services.RedisSortedSetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    @Autowired
    private RedisSortedSetsService redisSortedSetsService;

    @PostMapping("/{board}")
    public String addScore(@PathVariable String board, @RequestBody LeaderboardDto leaderboardDto) {
        redisSortedSetsService.addScore(board, leaderboardDto.getPlayer(), leaderboardDto.getScore());
        return "Score added for player: " + leaderboardDto.getPlayer();
    }

    @GetMapping("/{board}/top/{count}")
    public Set<ZSetOperations.TypedTuple<String>> getTopPlayers(@PathVariable String board, @PathVariable int count) {
        return redisSortedSetsService.getTopPlayers(board, count);
    }
}
