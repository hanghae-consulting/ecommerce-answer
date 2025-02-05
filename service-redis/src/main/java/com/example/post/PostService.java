package com.example.post;

import com.example.annotations.RedissonLock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final StringRedisTemplate redisTemplate;

    private final String keyPrefix = "post:like:";

    // 새 게시글 생성
    public Post createPost(String title, String content) {
        Post post = new Post(title, content);
        return postRepository.save(post);
    }

    /**
     * 좋아요 증가
     * - DB와 Redis를 동시에 업데이트
     * - 분산락 어노테이션(@RedissonLock) 적용
     */
    @RedissonLock(keyPrefix + "#postId")
    public void incrementLikeCount(Long postId) {
        String key = keyPrefix + postId;

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 게시글 ID: " + postId)
        );
        post.incrementLikeCount();
        postRepository.save(post);

        redisTemplate.opsForValue().increment(key);
    }

    // Redis에 누적된 좋아요 수 조회
    public Long getLikeCountInRedis(Long postId) {
        String key = keyPrefix + postId;
        String result = redisTemplate.opsForValue().get(key);
        if (result == null) return 0L;
        return Long.parseLong(result);
    }

    // DB에 저장된 좋아요 수 조회
    public Long getLikeCountInDB(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 게시글 ID: " + postId)
        );
        return post.getLikeCount();
    }
}
