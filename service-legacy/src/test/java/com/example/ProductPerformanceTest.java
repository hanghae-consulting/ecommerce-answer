package com.example;

import com.example.product.dto.ProductDto;
import com.example.product.entity.Product;
import com.example.product.service.CacheProductService;
import com.example.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductPerformanceTest {

    @Autowired
    private ProductService productService;

    private final List<String> productIds = new ArrayList<>();

    @BeforeAll
    public void setUpData() {
        String productName = "Product";
        long productPrice = 10000;

        int productCnt = 100000;
        for (int i = 1; i < productCnt; i++) {
            ProductDto dto = new ProductDto(productName + i, productPrice + i);
            Product product = productService.createProduct(dto);

            String productId = product.getProductId();
            productIds.add(productId);

            log.info("Created Product Id: {}", productId);
        }
        log.info("Created {} products", productCnt);
    }

    @Test
    @DisplayName("모든 상품 조회 : 레거시")
    public void testLegacyProductRetrieval(){
        long startTime = System.currentTimeMillis();

        for (String productId : productIds) {
            productService.getProduct(productId);
            log.info("Retrieved Product Id: {}", productId);
        }

        long endTime = System.currentTimeMillis();
        log.info("Legacy product retrieval time: {} ms", endTime - startTime);
    }

    @AfterAll
    public void cleanupData() {
        for (String productId : productIds) {
            productService.deleteProduct(productId);
        }
        log.info("Deleted all products.");
    }
}