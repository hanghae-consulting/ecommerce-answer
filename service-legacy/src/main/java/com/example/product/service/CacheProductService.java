package com.example.product.service;

import com.example.product.dto.ProductDto;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheProductService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;


    public Product createProduct(ProductDto dto) {
        Product product = Product.builder()
                .productId(generatedProductId())
                .name(dto.getName())
                .price(dto.getPrice())
                .build();

        Product savedProduct = productRepository.saveAndFlush(product);

        String cacheKey = generateCacheKey(savedProduct.getProductId());

        redisTemplate.opsForValue().set(cacheKey, savedProduct, 1, TimeUnit.HOURS);

        log.info("Product created and cached: {}", cacheKey);
        return savedProduct;
    }

    public Product getProduct(String productId) {
        String cacheKey = generateCacheKey(productId);

        Product cachedProduct = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProduct != null) {
            log.info("[RedisCache] Hit for key={},", cacheKey);
            return cachedProduct;
        }

        Product dbProduct = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found" + productId));

        redisTemplate.opsForValue().set(cacheKey, dbProduct, 1, TimeUnit.HOURS);

        log.info("Product retrieved from DB and cached: {}", cacheKey);
        return dbProduct;
    }

    public String generatedProductId(){
        return java.util.UUID.randomUUID().toString();
    }

    public String generateCacheKey(String productId){
        return "product:" + productId;
    }
}
