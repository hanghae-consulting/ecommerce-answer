package com.example.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderDto {
    private String storeId;
    private String productId;
    private String stockId;
    private Long quantity;
}
