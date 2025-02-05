package com.example.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockDto {
    private String storeId;
    private String productId;
    private Long stock;
}
