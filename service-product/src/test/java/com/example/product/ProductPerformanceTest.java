package com.example.product;

import com.example.kafka.CreateProductEvent;
import com.example.product.entity.Product;
import com.example.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductPerformanceTest {

    @Autowired
    private ProductService productService;

    // 생성할 상품 수
    private final int productCnt = 10000;

    // 생성된 상품 ID를 저장할 리스트
    private List<String> productIds;

    @BeforeAll
    public void setUpData() {
        String productName = "Product";
        long productPrice = 10000;

        // 초기 용량 설정으로 메모리 효율성 향상
        productIds = new ArrayList<>(productCnt);

        for (int i = 0; i < productCnt; i++) {
            String uuid = UUID.randomUUID().toString();
            CreateProductEvent event = new CreateProductEvent(
                    uuid,
                    productName + i,
                    productPrice
            );
            productService.createProduct(event);
            productIds.add(uuid);

            // 진행 상황 로그 (옵션)
            if (i % 1000000 == 0 && i != 0) {
                log.info("Created {} products", i);
            }
        }
        log.info("Created {} products", productCnt);
    }

    @Test
    @DisplayName("리스트를 이용한 모든 상품 조회: Kafka + Caching")
    public void testCacheProductRetrieval(){
        // 상품 수 확인
        assertEquals(productService.getProductCount(), productCnt, "상품 수가 일치하지 않습니다.");

        // 모든 상품을 리스트를 통해 조회
        for (String productId : productIds) {
            Product product = productService.getProduct(productId);
        }

        log.info("모든 상품을 성공적으로 조회하였습니다.");
    }
}
