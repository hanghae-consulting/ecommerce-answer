package com.example.products;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private static final String PRODUCT_KEY_PREFIX = "product:";

    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        String redisKey = PRODUCT_KEY_PREFIX + savedProduct.getId();
        redisTemplate.opsForValue().set(redisKey, product, 1, TimeUnit.HOURS);
        localCache.put(redisKey, savedProduct);
        System.out.println("Saved product: " + redisKey);
        return savedProduct;
    }


    public Optional<Product> getProduct(Long id) {
        String redisKey = PRODUCT_KEY_PREFIX + id;

        Product product = (Product) localCache.getIfPresent(redisKey);
        if (product != null){
            System.out.println("Local Cache hit for product: " + redisKey);
            return Optional.of(product);
        }

        product = (Product) redisTemplate.opsForValue().get(redisKey);
        if (product != null) {
            localCache.put(redisKey, product);
            System.out.println("Cache hit");
            return Optional.of(product);
        }

        Optional<Product> dbProduct = productRepository.findById(id);
        dbProduct.ifPresent(p -> {
            log.info("Cache hit for product: " + p.getId());
            localCache.put(redisKey, p);
            redisTemplate.opsForValue().set(redisKey, p);
        });

        return dbProduct;
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        if (!productRepository.existsById(id)){
            throw new IllegalArgumentException("Product not found for id: " + id);
        }

        updatedProduct.setId(id);
        Product savedProduct = productRepository.save(updatedProduct);
        String redisKey = PRODUCT_KEY_PREFIX + savedProduct.getId();

        redisTemplate.opsForValue().set(redisKey, savedProduct);
        redisTemplate.convertAndSend("cache-sync", "Updated product-" + redisKey);
        return savedProduct;
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)){
            throw new IllegalArgumentException("Product not found for id: " + id);
        }

        productRepository.deleteById(id);
        String productId = id.toString();
        String redisKey = PRODUCT_KEY_PREFIX + productId;

        redisTemplate.convertAndSend("cache-sync", "Deleted product-" + redisKey);
    }
}
