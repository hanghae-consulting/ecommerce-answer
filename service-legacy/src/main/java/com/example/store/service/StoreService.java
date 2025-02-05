package com.example.store.service;

import com.example.store.dto.StoreDto;
import com.example.store.entity.Store;
import com.example.store.repository.StoreRepository;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final ObjectMapper mapper;

    public Store createStore(StoreDto dto) {
        Store store = mapper.convertValue(dto, Store.class);
        String uuid = UUID.randomUUID().toString();
        store.setStoreId(uuid);
        return storeRepository.saveAndFlush(store);
    }

    public Store updateStore(String storeId, StoreDto dto) throws JsonMappingException {
        Store store = this.getStore(storeId);
        mapper.updateValue(store, dto);

        return storeRepository.save(store);
    }

    public void deleteStore(String storeId) {
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));
        storeRepository.delete(store);
    }

    public Store getStore(String storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));
    }
}
