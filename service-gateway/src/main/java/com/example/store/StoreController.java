package com.example.store;

import com.example.store.dto.StoreRequest;
import com.example.store.dto.StoreResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreFeignClient storeFeignClient;

    public StoreController(StoreFeignClient storeFeignClient) {
        this.storeFeignClient = storeFeignClient;
    }

    @PostMapping
    public String createStore(@RequestBody StoreRequest request) {
        return storeFeignClient.createStore(request);
    }

    @GetMapping("/{storeId}")
    public StoreResponse getStore(@PathVariable String storeId) {
        return storeFeignClient.getStore(storeId);
    }

    @PutMapping("/{storeId}")
    public boolean updateStore(@PathVariable String storeId, @RequestBody StoreRequest request) {
        return storeFeignClient.updateStore(storeId, request);
    }

    @DeleteMapping("/{storeId}")
    public boolean deleteStore(@PathVariable String storeId) {
        return storeFeignClient.deleteStore(storeId);
    }
}