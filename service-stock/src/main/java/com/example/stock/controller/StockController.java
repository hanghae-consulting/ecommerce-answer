package com.example.stock.controller;

import com.example.kafka.CreateStockEvent;
import com.example.kafka.DecreaseStockEvent;
import com.example.kafka.DeleteStockEvent;
import com.example.kafka.UpdateStockEvent;
import com.example.stock.dto.StockDto;
import com.example.stock.entity.Stock;
import com.example.stock.kafka.StockEventProducer;
import com.example.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockEventProducer eventProducer;
    private final StockService stockService;

    @PostMapping
    public String createStock(@RequestBody StockDto dto) {
        log.info("Create stock: {}", dto);
        // CreateStockEvent 발행
        String uuid = UUID.randomUUID().toString();
        CreateStockEvent event = new CreateStockEvent(
                uuid,
                dto.getStoreId(),
                dto.getProductId(),
                dto.getStock()
        );
        eventProducer.sendCommandEvent(event);

        return uuid;
    }

    @GetMapping("/{stockId}")
    public Stock getStock(@PathVariable String stockId) {
        log.info("Get stock by id: {}", stockId);
        return stockService.getStock(stockId);
    }

    @PutMapping("/{stockId}")
    public boolean updateStock(@PathVariable String stockId,
                              @RequestBody StockDto dto) {
        log.info("Update stock: {}", dto);
        UpdateStockEvent event = new UpdateStockEvent(
                stockId,
                dto.getStoreId(),
                dto.getProductId(),
                dto.getStock()
        );
        eventProducer.sendCommandEvent(event);

        return true;
    }

    @PutMapping("/{stockId}/decrease/{quantity}")
    public boolean decreaseStock(@PathVariable String stockId,
                                 @PathVariable Long quantity
                               ) {
        DecreaseStockEvent event = new DecreaseStockEvent(
                stockId,
                quantity
        );
        eventProducer.sendCommandEvent(event);

        return true;
    }

    @DeleteMapping("/{stockId}")
    public boolean deleteStock(@PathVariable String stockId) {
        DeleteStockEvent event = new DeleteStockEvent(stockId);
        eventProducer.sendCommandEvent(event);

        return true;
    }
}
