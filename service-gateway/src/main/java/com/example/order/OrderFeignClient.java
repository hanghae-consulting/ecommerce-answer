package com.example.order;

import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "orderClient", url = "http://localhost:8083")
public interface OrderFeignClient {

    // [C] Create
    @PostMapping("/api/order")
    String createOrder(@RequestBody OrderRequest request);

    // [R1] 단건 조회
    @GetMapping("/api/order/{orderId}")
    OrderResponse getOrder(@PathVariable("orderId") String orderId);

    // [U] Update
    @PutMapping("/api/order/{orderId}")
    boolean updateOrders(
            @PathVariable("orderId") String orderId,
            @RequestBody OrderRequest request
    );

    // [D] Delete
    @DeleteMapping("/api/order/{orderId}")
    boolean deleteOrder(@PathVariable("orderId") String orderId);
}
