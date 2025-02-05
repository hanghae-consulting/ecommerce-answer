package com.example.product;

import com.example.annotations.RedissonLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    private final String Stock_KEY = "#StockId-5a67de67-6f81-4921-b301-ca4109211fa1";

    private final StockRepository StockRepository;

    public void Stocking(Long StockId, Long quantity){

        Stock Stock = StockRepository.findById(StockId).orElseThrow();
        Stock.decrease(quantity);
        log.info("quantity: {}, after quantity: {}", quantity, Stock.getQuantity());
        StockRepository.saveAndFlush(Stock);
    }

    public void normalStocking(Long StockId, Long quantity){
        Stocking(StockId, quantity);
    }

    @RedissonLock(value = Stock_KEY)
    public void redissonStocking(Long StockId, Long quantity){
        Stocking(StockId, quantity);
    }
}
