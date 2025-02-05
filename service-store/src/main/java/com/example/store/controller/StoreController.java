package com.example.store.controller;

import com.example.store.entity.Store;
import com.example.kafka.CreateStoreEvent;
import com.example.kafka.DeleteStoreEvent;
import com.example.kafka.UpdateStoreEvent;
import com.example.store.dto.StoreDto;
import com.example.store.kafka.StoreEventProducer;
import com.example.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {
    private final StoreEventProducer eventProducer;
    private final StoreService storeService;

    @PostMapping
    public String createStore(@RequestBody StoreDto dto) {
        String uuid = UUID.randomUUID().toString();
        // CreateStoreEvent 발행
        CreateStoreEvent event = new CreateStoreEvent(
                uuid,
                dto.getStoreName(),
                dto.getOwnerName(),
                dto.getAddress(),
                dto.getPhoneNumber()
        );
        eventProducer.sendCommandEvent(event);

        return uuid;
    }

    @GetMapping("/{storeId}")
    public Store getStore(@PathVariable String storeId) {
        return storeService.getStore(storeId);
    }

    /**
     * [U] 스토어 수정
     */
    @PutMapping("/{storeId}")
    public boolean updateStore(@PathVariable String storeId,
                              @RequestBody StoreDto dto) {
        UpdateStoreEvent event = new UpdateStoreEvent(
                storeId,
                dto.getStoreName(),
                dto.getOwnerName(),
                dto.getAddress(),
                dto.getPhoneNumber()
        );
        eventProducer.sendCommandEvent(event);

        return true;
    }

    /**
     * [D] 스토어 삭제
     */
    @DeleteMapping("/{storeId}")
    public boolean deleteStore(@PathVariable String storeId) {
        DeleteStoreEvent event = new DeleteStoreEvent(storeId);
        eventProducer.sendCommandEvent(event);

        return true;
    }
}
