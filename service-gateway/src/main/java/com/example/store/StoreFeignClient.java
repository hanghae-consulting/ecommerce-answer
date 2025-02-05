package com.example.store;

import com.example.config.FeignConfig;
import com.example.store.dto.StoreRequest;
import com.example.store.dto.StoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "storeClient", url = "http://localhost:8081", configuration = FeignConfig.class)
public interface StoreFeignClient {

    @PostMapping("/api/store")
    String createStore(@RequestBody StoreRequest request);

    @GetMapping("/api/store/{storeId}")
    StoreResponse getStore(@PathVariable("storeId") String storeId);

    @PutMapping("/api/store/{storeId}")
    boolean updateStore(@PathVariable("storeId") String storeId, @RequestBody StoreRequest request);

    @DeleteMapping("/api/store/{storeId}")
    boolean deleteStore(@PathVariable("storeId") String storeId);
}