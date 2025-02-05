package com.example.order.controller;

import com.example.order.dto.OrderDto;
import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody OrderDto dto) {
        return orderService.createOrder(dto);
    }

    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable String orderId) {
        return orderService.getOrder(orderId);
    }

    @PutMapping("/{orderId}")
    public Order updateOrder(@PathVariable String orderId,
                              @RequestBody OrderDto dto) throws JsonMappingException {

        return orderService.updateOrder(orderId, dto);
    }

    @DeleteMapping("/{orderId}")
    public boolean deleteOrder(@PathVariable String orderId) {
        orderService.deleteOrder(orderId);

        return true;
    }
}
