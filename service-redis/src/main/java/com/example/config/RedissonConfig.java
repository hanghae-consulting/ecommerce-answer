package com.example.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress(
                        "redis://127.0.0.1:7001",
                        "redis://127.0.0.1:7002",
                        "redis://127.0.0.1:7003",
                        "redis://127.0.0.1:7004",
                        "redis://127.0.0.1:7005",
                        "redis://127.0.0.1:7006"
                ).setScanInterval(2000);

        return Redisson.create(config);
    }
}
