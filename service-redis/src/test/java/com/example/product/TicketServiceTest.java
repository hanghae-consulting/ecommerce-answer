package com.example.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class StockServiceTest {

    @Autowired
    StockService StockService;

    @Autowired
    StockRepository StockRepository;

    private Long Stock_ID = null;

    @BeforeEach
    public void before() {
        log.info("1000개의 티켓 생성");
        Stock Stock = Stock.create(1000L);
        Stock saved = StockRepository.saveAndFlush(Stock);
        Stock_ID = saved.getId();
        log.info("StockId: {}", Stock_ID);
    }

    @AfterEach
    public void after(){
        StockRepository.deleteAll();
    }

    private void StockingTest(Consumer<Void> action) throws InterruptedException {
        log.info("Stocking Test");
        Long originQuantity = StockRepository.findById(Stock_ID).orElseThrow().getQuantity();
        log.info("originQuantity: {}", originQuantity);

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        Integer CONCURRENT_COUNT = 100;
        CountDownLatch latch = new CountDownLatch(CONCURRENT_COUNT);

        for (int i = 0; i < CONCURRENT_COUNT; i++){
            executorService.submit(() -> {
                try{
                    action.accept(null);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock Stock = StockRepository.findById(Stock_ID).orElseThrow();
        assertEquals(originQuantity - CONCURRENT_COUNT, Stock.getQuantity());
    }

    @Test
    @DisplayName("동시에 100명의 티켓팅 : 동시성 이슈")
    public void badStockingTest() throws Exception {
        StockingTest((_no) -> StockService.normalStocking(Stock_ID, 1L));
    }

    @Test
    @DisplayName("동시에 100명의 티켓팅 : 분산락")
    public void redissonStockingTest() throws Exception {
        StockingTest((_no) -> StockService.redissonStocking(Stock_ID, 1L));
    }
}
