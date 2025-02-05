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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository ProductRepository;
    private final ObjectMapper mapper;


    public Product createProduct(ProductDto dto) {
        Product product = mapper.convertValue(dto, Product.class);
        String uuid = UUID.randomUUID().toString();
        product.setProductId(uuid);

        return ProductRepository.saveAndFlush(product);
    }

    public Product getProduct(String productId) {
        log.info("Get product by id: {}", productId);
        return ProductRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    public Product updateProduct(String productId, ProductDto dto) throws JsonMappingException {
        Product product = this.getProduct(productId);
        mapper.updateValue(product, dto);
        return ProductRepository.save(product);
    }

    public void deleteProduct(String productId) {
        Product Product = ProductRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        ProductRepository.delete(Product);
    }

}
