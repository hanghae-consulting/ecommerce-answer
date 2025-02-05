package com.example.product.controller;

import com.example.kafka.*;
import com.example.product.dto.ProductDto;
import com.example.product.dto.ProductMetricsDto;
import com.example.product.entity.Product;
import com.example.product.kafka.ProductEventProducer;
import com.example.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductEventProducer eventProducer;
    private final ProductService productService;

    @PostMapping
    public String createProduct(@RequestBody ProductDto dto) {
        // CreateProductEvent 발행
        String uuid = UUID.randomUUID().toString();
        CreateProductEvent event = new CreateProductEvent(
                uuid,
                dto.getName(),
                dto.getPrice()
        );
        eventProducer.sendCommandEvent(event);

        return uuid;
    }

    @GetMapping("/{productId}")
    public Product getProduct(@PathVariable String productId) {
        return productService.getProduct(productId);
    }

    @PutMapping("/{productId}")
    public boolean updateProduct(@PathVariable String productId,
                              @RequestBody ProductDto dto) {
        UpdateProductEvent event = new UpdateProductEvent(
                productId,
                dto.getName(),
                dto.getPrice()
        );
        eventProducer.sendCommandEvent(event);

        return true;
    }

    @DeleteMapping("/{productId}")
    public boolean deleteProduct(@PathVariable String productId) {
        DeleteProductEvent event = new DeleteProductEvent(productId);
        eventProducer.sendCommandEvent(event);

        return true;
    }

    @PostMapping("/like/{productId}/username/{username}")
    public String likeProduct(@PathVariable String productId, @PathVariable String username) {
        productService.likeProduct(productId, username);
        return "LikeProductEvent published. productId=" + productId;
    }

    @PostMapping("/visit/{productId}")
    public String visitProduct(@PathVariable String productId) {
        productService.visitProduct(productId);
        return "VisitProductEvent published. productId=" + productId;
    }

    @PostMapping("/search/{productId}")
    public String searchProduct(@PathVariable String productId, @RequestParam String query) {
        productService.searchProduct(productId, query);
        return "SearchProductEvent published. productId=" + productId + ", query=" + query;
    }

    @GetMapping("/metrics/{productId}")
    public ProductMetricsDto getProductDetails(@PathVariable String productId) {
        return productService.getProductMetrics(productId);
    }
}
