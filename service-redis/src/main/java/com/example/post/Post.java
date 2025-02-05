package com.example.post;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    // 기본값 0으로 초기화
    private Long likeCount = 0L;

    @Builder
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
        // likeCount는 선언 시점에 0으로 초기화하므로 여기서는 별도 초기화 없음
    }

    // 좋아요 수를 1 증가
    public void incrementLikeCount() {
        this.likeCount++;
    }
}
