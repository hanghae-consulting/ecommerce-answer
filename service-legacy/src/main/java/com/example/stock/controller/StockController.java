package com.example.stock.controller;

import com.example.stock.dto.StockDto;
import com.example.stock.entity.Stock;
import com.example.stock.service.StockService;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public Stock createStock(@RequestBody StockDto dto) {
        return stockService.createStock(dto);
    }

    @GetMapping("/{stockId}")
    public Stock getStock(@PathVariable String stockId) {
        log.info("Get stock by id: {}", stockId);
        return stockService.getStock(stockId);
    }

    @PutMapping("/{stockId}")
    public Stock updateStock(@PathVariable String stockId,
                              @RequestBody StockDto dto) throws JsonMappingException {
        return stockService.updateStock(stockId, dto);
    }

    @PutMapping("/{stockId}/decrease/{quantity}")
    public Stock decreaseStock(@PathVariable String stockId,
                                 @PathVariable Long quantity
                               ) {
        return stockService.decreaseStock(stockId, quantity);
    }

    @DeleteMapping("/{stockId}")
    public boolean deleteStock(@PathVariable String stockId) {
        stockService.deleteStock(stockId);

        return true;
    }
}
