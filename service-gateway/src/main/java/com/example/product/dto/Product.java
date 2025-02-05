package com.example.product.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private Long id;

    private String productId;
    private String name;
    private Long price;
}
