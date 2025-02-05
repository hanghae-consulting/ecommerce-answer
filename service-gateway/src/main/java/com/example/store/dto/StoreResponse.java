package com.example.store.dto;

import lombok.Data;

@Data
public class StoreResponse {
    private Long id;
    private String storeId;
    private String storeName;
    private String ownerName;
    private String address;
    private String phoneNumber;
}
