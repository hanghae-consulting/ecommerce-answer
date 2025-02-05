package com.example.redis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RedisGeospatialService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public DistanceUnit getDistanceUnit(String unit) {
        return switch (unit.toLowerCase()) {
            case "km" -> DistanceUnit.KILOMETERS;
            case "m" -> DistanceUnit.METERS;
            case "mi" -> DistanceUnit.MILES;
            case "ft" -> DistanceUnit.FEET;
            default -> throw new IllegalArgumentException("Unsupported distance unit: " + unit);
        };
    }

    public void addLocation(String key, double longitude, double latitude, String name){
        redisTemplate.opsForGeo().add(key, new RedisGeoCommands.GeoLocation<>(name, new Point(longitude, latitude)));
    }

    public Double getDistance(String key, String from, String to, String unit) {
        DistanceUnit distanceUnit = getDistanceUnit(unit);

        return Objects.requireNonNull(redisTemplate.opsForGeo().distance(key, from, to, distanceUnit)).getValue();
    }

    public List<RedisGeoCommands.GeoLocation<String>> getNearbyLocations(String key, double longitude, double latitude, double radius, String unit) {
        DistanceUnit distanceUnit = getDistanceUnit(unit);
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        return Objects.requireNonNull(geoOps.radius(key, new Circle(new Point(longitude, latitude), new Distance(radius, distanceUnit))))
                .getContent().stream()
                .map(GeoResult::getContent)
                .collect(Collectors.toList());
    }
}
