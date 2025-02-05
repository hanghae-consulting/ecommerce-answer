package com.example.order.service;

import com.example.order.dto.OrderDto;
import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository OrderRepository;
    private final ObjectMapper mapper;

    public Order createOrder(OrderDto dto) {
        Order order = mapper.convertValue(dto, Order.class);
        String uuid = UUID.randomUUID().toString();
        order.setOrderId(uuid);
        return OrderRepository.saveAndFlush(order);
    }

    public Order updateOrder(String orderId, OrderDto dto) throws JsonMappingException {
        Order order = this.getOrder(orderId);
        mapper.updateValue(order, dto);
        return OrderRepository.save(order);
    }


    public void deleteOrder(String orderId) {
        Order Order = OrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        OrderRepository.delete(Order);
    }

    public Order getOrder(String orderId) {
        return OrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }
}
