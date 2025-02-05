package com.example.store.dto;

import lombok.Data;

@Data
public class StoreRequest {
    private String storeName;
    private String ownerName;
    private String address;
    private String phoneNumber;
}
