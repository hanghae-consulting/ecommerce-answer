package com.example.redis.controllers;

import com.example.redis.services.RedisGeospatialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/geospatial")
public class GeospatialController {

    @Autowired
    private RedisGeospatialService redisGeospatialService;

    @PostMapping("/{key}")
    public String addLocation(@PathVariable String key,
                              @RequestParam double longitude,
                              @RequestParam double latitude,
                              @RequestParam String name
    ){
        redisGeospatialService.addLocation(key, longitude, latitude, name);
        return "Location added: " + name;
    }

    @GetMapping("/{key}/distance")
    public String getDistance(@PathVariable String key,
                              @RequestParam String from,
                              @RequestParam String to,
                              @RequestParam String unit) {
        return "Distance: " + redisGeospatialService.getDistance(key, from, to, unit) + " " + unit;
    }

    @GetMapping("/{key}/nearby")
    public List<RedisGeoCommands.GeoLocation<String>> getNearbyLocation(@PathVariable String key,
                                                                        @RequestParam double longitude,
                                                                        @RequestParam double latitude,
                                                                        @RequestParam double radius,
                                                                        @RequestParam String unit){
        return redisGeospatialService.getNearbyLocations(key, longitude, latitude, radius, unit);
    }
}
