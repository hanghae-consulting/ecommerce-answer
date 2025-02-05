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
public class StockServiceWaveTest {

    @Autowired
    StockService StockService;

    @Autowired
    StockRepository StockRepository;

    private Long Stock_ID = null;

    @BeforeEach
    public void before() {
        log.info("===== 10000개의 티켓 생성 (WaveTest) =====");
        Stock Stock = Stock.create(10000L); // 재고를 10000으로 설정
        Stock saved = StockRepository.saveAndFlush(Stock);
        Stock_ID = saved.getId();
        log.info("StockId: {}", Stock_ID);
    }

    @AfterEach
    public void after() {
        StockRepository.deleteAll();
    }

    /**
     *  웨이브(라운드) 테스트 메서드
     *  - waveCount : 웨이브(라운드) 수
     *  - concurrencyPerWave : 각 웨이브마다 동시에 발생하는 주문 수
     *  - 각 웨이브가 끝날 때마다 0.5 ~ 1.5초 랜덤 지연
     */
    private void StockingTestWave(Consumer<Void> action, int waveCount, int concurrencyPerWave) throws InterruptedException {
        log.info("===== StockingTestWave start (waveCount={}, concurrencyPerWave={}) =====", waveCount, concurrencyPerWave);

        Long originQuantity = StockRepository.findById(Stock_ID).orElseThrow().getQuantity();
        log.info("originQuantity: {}", originQuantity);

        // 병렬처리를 위한 ExecutorService
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // N번의 웨이브 반복
        for (int wave = 1; wave <= waveCount; wave++) {
            log.info("===== Wave {} Start =====", wave);
            CountDownLatch latch = new CountDownLatch(concurrencyPerWave);

            for (int i = 0; i < concurrencyPerWave; i++) {
                executorService.submit(() -> {
                    try {
                        action.accept(null);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 해당 웨이브의 모든 스레드가 끝날 때까지 대기
            latch.await();
            log.info("===== Wave {} Done =====", wave);

            // 웨이브 사이에 0.5 ~ 1.5초 랜덤 지연
            long sleepTime = (long) (500 + Math.random() * 1000); // 500 ~ 1500ms
            Thread.sleep(sleepTime);
        }

        // 모든 웨이브가 끝난 뒤 재고 확인
        executorService.shutdown();
        Stock Stock = StockRepository.findById(Stock_ID).orElseThrow();

        long totalOrders = (long) waveCount * concurrencyPerWave;
        // 기대값: originQuantity - totalOrders
        long expected = originQuantity - totalOrders;
        long actual = Stock.getQuantity();

        log.info("===== All waves done. expected={} / actual={} =====", expected, actual);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("WaveTest - 5번 웨이브 × 각 1000명 동시 : 동시성 이슈")
    public void badStockingWaveTest() throws Exception {
        int waveCount = 5;
        int concurrencyPerWave = 1000;
        StockingTestWave((_no) -> StockService.normalStocking(Stock_ID, 1L),
                waveCount,
                concurrencyPerWave);
    }

    @Test
    @DisplayName("WaveTest - 5번 웨이브 × 각 1000명 동시 : 분산락")
    public void redissonStockingWaveTest() throws Exception {
        int waveCount = 5;
        int concurrencyPerWave = 1000;
        // 실행하면 총 5000건 주문 발생
        StockingTestWave((_no) -> StockService.redissonStocking(Stock_ID, 1L),
                          waveCount,
                          concurrencyPerWave);
    }
}
