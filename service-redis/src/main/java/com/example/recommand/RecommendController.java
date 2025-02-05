package com.example.recommand;

import com.example.recommand.services.RecommendGeospatialService;
import com.example.recommand.services.RecommendHyperLogLogService;
import com.example.recommand.services.RecommendSortedSetsService;
import com.example.recommand.services.RecommendStreamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/recommendation")
public class RecommendController {

    @Autowired
    private RecommendStreamsService recommendStreamsService;

    @Autowired
    private RecommendSortedSetsService recommendSortedSetsService;

    @Autowired
    private RecommendHyperLogLogService recommendHyperLogLogService;

    @Autowired
    private RecommendGeospatialService recommendGeospatialService;

    // 사용자 이벤트 추가
    @PostMapping("/event")
    public String addEvent(@RequestParam String userId, @RequestParam String action, @RequestParam String itemId) {
        recommendStreamsService.addEvent("user_events", userId, action, itemId);

        // 인기도 업데이트 (클릭: +1, 좋아요: +5)
        int score = action.equalsIgnoreCase("like") ? 5 : 1;
        recommendSortedSetsService.updatePopularity("item_leaderboard", itemId, score);

        // 고유 사용자 추가
        recommendHyperLogLogService.addUser("unique_users:" + itemId, userId);

        return "Event added";
    }

    // 인기 상품 조회
    @GetMapping("/top-items")
    public Set<String> getTopItems(@RequestParam int count) {
        return recommendSortedSetsService.getTopItems("item_leaderboard", count);
    }

    // 특정 상품의 고유 사용자 수 조회
    @GetMapping("/unique-users/{itemId}")
    public long getUniqueUsers(@PathVariable String itemId) {
        return recommendHyperLogLogService.getUniqueUsers("unique_users:" + itemId);
    }

    // 상점 위치 추가
    @PostMapping("/store")
    public String addStore(@RequestParam String name, @RequestParam double longitude, @RequestParam double latitude) {
        recommendGeospatialService.addLocation("store_locations", name, longitude, latitude);
        return "Store location added";
    }

    // 특정 반경 내 위치 검색
    @GetMapping("/nearby-stores")
    public List<RedisGeoCommands.GeoLocation<String>> getNearbyStores(@RequestParam double longitude, @RequestParam double latitude, @RequestParam double radius, @RequestParam String unit) {
        return recommendGeospatialService.getNearbyLocations("store_locations", longitude, latitude, radius, unit);
    }
}
