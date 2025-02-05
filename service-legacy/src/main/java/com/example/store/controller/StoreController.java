package com.example.store.controller;

import com.example.store.dto.StoreDto;
import com.example.store.entity.Store;
import com.example.store.service.StoreService;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public Store createStore(@RequestBody StoreDto dto) {
        return storeService.createStore(dto);
    }

    @GetMapping("/{storeId}")
    public Store getStore(@PathVariable String storeId) {
        return storeService.getStore(storeId);
    }

    /**
     * [U] 스토어 수정
     */
    @PutMapping("/{storeId}")
    public Store updateStore(@PathVariable String storeId,
                              @RequestBody StoreDto dto) throws JsonMappingException {
        return storeService.updateStore(storeId, dto);
    }

    /**
     * [D] 스토어 삭제
     */
    @DeleteMapping("/{storeId}")
    public boolean deleteStore(@PathVariable String storeId) {
        storeService.deleteStore(storeId);

        return true;
    }
}
