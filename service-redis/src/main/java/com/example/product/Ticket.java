package com.example.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@NoArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private Long quantity;

    public static Stock create(Long quantity) {
        Stock entity = new Stock();
        entity.setQuantity(quantity);
        return entity;
    }

    public void decrease(Long quantity) {
        long q = this.quantity - quantity;
        this.quantity = q < 0 ? 0L : q;
    }

}