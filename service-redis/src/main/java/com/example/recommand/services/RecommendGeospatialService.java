package com.example.recommand.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RecommendGeospatialService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 단위를 DistanceUnit으로 변환
    public RedisGeoCommands.DistanceUnit getDistanceUnit(String unit){
        return switch (unit.toLowerCase()) {
            case "km" -> RedisGeoCommands.DistanceUnit.KILOMETERS;
            case "m" -> RedisGeoCommands.DistanceUnit.METERS;
            case "mi" -> RedisGeoCommands.DistanceUnit.MILES;
            case "ft" -> RedisGeoCommands.DistanceUnit.FEET;
            default -> throw new IllegalArgumentException("Unsupported distance unit: " + unit);
        };
    }

    // 상점 위치 추가
    public void addLocation(String key, String locationName, double longitude, double latitude) {
        redisTemplate.opsForGeo().add(key, new Point(longitude, latitude), locationName);
    }

    // 특정 반경 내 위치 검색
    public List<RedisGeoCommands.GeoLocation<String>> getNearbyLocations(String key, double longitude, double latitude, double radius, String unit) {
        RedisGeoCommands.DistanceUnit distanceUnit = getDistanceUnit(unit);
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        return Objects.requireNonNull(geoOps.radius(key, new Circle(new Point(longitude, latitude), new Distance(radius, distanceUnit))))
                .getContent().stream()
                .map(GeoResult::getContent)
                .collect(Collectors.toList());
    }
}
