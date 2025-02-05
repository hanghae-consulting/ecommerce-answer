package com.example.order.controller;

import com.example.kafka.CreateOrderEvent;
import com.example.kafka.DeleteOrderEvent;
import com.example.kafka.UpdateOrderEvent;
import com.example.order.dto.OrderDto;
import com.example.order.entity.Order;
import com.example.order.kafka.OrderEventProducer;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderEventProducer eventProducer;
    private final OrderService orderService;

    @PostMapping
    public String createOrder(@RequestBody OrderDto dto) {
        // CreateOrderEvent 발행
        String uuid = UUID.randomUUID().toString();
        CreateOrderEvent event = new CreateOrderEvent(
                uuid,
                dto.getStoreId(),
                dto.getProductId(),
                dto.getStockId(),
                dto.getQuantity()
        );
        eventProducer.sendCommandEvent(event);

        return uuid;
    }

    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable String orderId) {
        return orderService.getOrder(orderId);
    }

    @PutMapping("/{orderId}")
    public boolean updateOrder(@PathVariable String orderId,
                              @RequestBody OrderDto dto) {
        UpdateOrderEvent event = new UpdateOrderEvent(
                orderId,
                dto.getStoreId(),
                dto.getProductId(),
                dto.getStockId(),
                dto.getQuantity()
        );
        eventProducer.sendCommandEvent(event);

        return true;
    }

    @DeleteMapping("/{orderId}")
    public boolean deleteOrder(@PathVariable String orderId) {
        DeleteOrderEvent event = new DeleteOrderEvent(orderId);
        eventProducer.sendCommandEvent(event);

        return true;
    }
}
