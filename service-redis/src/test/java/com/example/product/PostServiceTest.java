package com.example.product;

import com.example.post.Post;
import com.example.post.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PostServiceTest {

    @Autowired
    private PostService postService;

    private Long postId;

    @BeforeEach
    void setUp() {
        // 테스트용 게시글 하나 생성
        Post post = postService.createPost("Test Title", "Test Content");
        this.postId = post.getId();
    }

    @Test
    @DisplayName("동시성 테스트: 1000명이 동시에 좋아요를 눌렀을 때, 최종 좋아요 수가 1000인지 확인")
    void concurrencyLikeTest() throws InterruptedException {
        int concurrency = 1000;     // 동시 요청 수
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(concurrency);

        // 1000개의 스레드가 동시에 incrementLikeCount() 실행
        for (int i = 0; i < concurrency; i++) {
            executor.submit(() -> {
                try {
                    postService.incrementLikeCount(postId);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드 종료까지 대기
        latch.await();
        executor.shutdown();

        // 최종적으로 Redis와 DB의 좋아요 수가 1000이 되어야 함
        Long redisLikeCount = postService.getLikeCountInRedis(postId);
        Long dbLikeCount = postService.getLikeCountInDB(postId);

        System.out.println("Redis likeCount: " + redisLikeCount);
        System.out.println("DB likeCount: " + dbLikeCount);

        assertEquals(1000L, redisLikeCount, "Redis의 좋아요 수가 1000이 되어야 합니다.");
        assertEquals(1000L, dbLikeCount, "DB의 좋아요 수가 1000이 되어야 합니다.");
    }
}
