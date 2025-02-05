package com.example.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StoreDto {
    private String storeName;
    private String ownerName;
    private String address;
    private String phoneNumber;
}
