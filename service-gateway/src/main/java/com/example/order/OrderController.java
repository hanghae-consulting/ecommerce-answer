package com.example.order;

import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderFeignClient orderFeignClient;

    public OrderController(OrderFeignClient orderFeignClient) {
        this.orderFeignClient = orderFeignClient;
    }

    @PostMapping
    public String createOrder(@RequestBody OrderRequest request) {
        return orderFeignClient.createOrder(request);
    }

    @GetMapping("/{storeId}")
    public OrderResponse getStore(@PathVariable String storeId) {
        log.info("Get store with id {}", storeId);
        return orderFeignClient.getOrder(storeId);
    }

    @PutMapping("/{orderId}")
    public boolean updateStore(
            @PathVariable String orderId,
            @RequestBody OrderRequest request
    ) {
        return orderFeignClient.updateOrders(orderId, request);
    }

    @DeleteMapping("/{orderId}")
    public boolean deleteStore(@PathVariable String orderId) {
        return orderFeignClient.deleteOrder(orderId);
    }
}
