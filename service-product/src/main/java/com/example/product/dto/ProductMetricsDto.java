package com.example.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductMetricsDto {
    private String productId;
    private Long likesCount;
    private Long visitsCount;
    private List<String> recentSearches;
}
