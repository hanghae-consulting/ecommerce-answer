package com.example.stock.service;

import com.example.stock.dto.StockDto;
import com.example.stock.entity.Stock;
import com.example.stock.repository.StockRepository;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ObjectMapper mapper;

    public Stock createStock(StockDto dto) {
        Stock stock = mapper.convertValue(dto, Stock.class);
        String uuid = UUID.randomUUID().toString();
        stock.setStockId(uuid);

        return stockRepository.saveAndFlush(stock);
    }

    public Stock getStock(String stockId) {
        return stockRepository.findByStockId(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));
    }

    public Stock updateStock(String stockId, StockDto dto) throws JsonMappingException {
        Stock stock = this.getStock(stockId);
        mapper.updateValue(stock, dto);
        return stockRepository.save(stock);
    }

    @Transactional
    public Stock decreaseStock(String stockId, Long quantity) {
        Stock stock = stockRepository.findByStockId(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));

        if (!stock.decrease(quantity)) throw new RuntimeException("The quantity is larger than the stock: " + stockId);

        return stockRepository.save(stock);
    }

    public void deleteStock(String stockId) {
        Stock stock = stockRepository.findByStockId(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));
        stockRepository.delete(stock);
    }
}
