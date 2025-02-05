package com.example.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Stock implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String stockId;
    private String storeId;
    private String productId;
    private long stock; // 기본형 사용

    public boolean decrease(long quantity) {
        return stock >= quantity && (stock -= quantity) >= 0;
    }
}
