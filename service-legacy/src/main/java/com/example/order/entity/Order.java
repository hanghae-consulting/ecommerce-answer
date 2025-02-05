package com.example.order.entity;

import jakarta.persistence.*;
        import lombok.*;

        import java.io.Serializable;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderId;
    private String storeId;
    private String productId;
    private String stockId;
    private Long quantity;
}
