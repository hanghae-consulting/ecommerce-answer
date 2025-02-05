package com.example.product;

import com.example.config.FeignConfig;
import com.example.product.dto.ProductMetricsDto;
import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "productClient", url = "http://localhost:8082")
public interface ProductFeignClient {

    @PostMapping("/api/product")
    String createProduct(@RequestBody ProductRequest request);

    @GetMapping("/api/product/{productId}")
    ProductResponse getProduct(@PathVariable("productId") String productId);

    @PutMapping("/api/product/{productId}")
    boolean updateProducts(
            @PathVariable("productId") String productId,
            @RequestBody ProductRequest request
    );

    @DeleteMapping("/api/product/{productId}")
    boolean deleteProduct(@PathVariable("productId") String productId);

    @PostMapping("/api/product/like/{productId}/username/{username}")
    String likeProduct(@PathVariable("productId") String productId, @PathVariable String username);

    @PostMapping("/api/product/visit/{productId}")
    String visitProduct(@PathVariable("productId") String productId);

    @PostMapping("/api/product/search/{productId}")
    String searchProduct(@PathVariable("productId") String productId, @RequestParam String query);

    @GetMapping("/api/product/metrics/{productId}")
    ProductMetricsDto getProductMetrics(@PathVariable String productId);
}
