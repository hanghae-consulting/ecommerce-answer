package com.example.product;

import com.example.product.dto.ProductMetricsDto;
import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductFeignClient productFeignClient;

    public ProductController(ProductFeignClient productFeignClient) {
        this.productFeignClient = productFeignClient;
    }

    @PostMapping
    public String createProduct(@RequestBody ProductRequest request) {
        return productFeignClient.createProduct(request);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable String productId) {
        return productFeignClient.getProduct(productId);
    }

    @PutMapping("/{productId}")
    public boolean updateProduct(
            @PathVariable String productId,
            @RequestBody ProductRequest request
    ) {
        return productFeignClient.updateProducts(productId, request);
    }

    @DeleteMapping("/{productId}")
    public boolean deleteStore(@PathVariable String productId) {
        return productFeignClient.deleteProduct(productId);
    }

    @PostMapping("/like/{productId}/username/{username}")
    public String likeProduct(@PathVariable String productId, @PathVariable String username) {
        return productFeignClient.likeProduct(productId, username);
    }

    @PostMapping("/visit/{productId}")
    public String visitProduct(@PathVariable String productId) {
        return productFeignClient.visitProduct(productId);
    }

    @PostMapping("/search/{productId}")
    public String searchProduct(@PathVariable String productId, @RequestParam String query) {
        return productFeignClient.searchProduct(productId, query);
    }

    @GetMapping("/metrics/{productId}")
    public ProductMetricsDto getProductMetrics(@PathVariable String productId) {
        return productFeignClient.getProductMetrics(productId);
    }
}
