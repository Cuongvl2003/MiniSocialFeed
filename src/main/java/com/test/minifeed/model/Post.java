package com.test.minifeed.model;

import java.time.Instant;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class Post {
    private long postId;
    private long userId;
    private String content;
    private Instant createdAt;
    public Post(Long postId, Long userId, String content, Instant createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }
    public static Post fromMap(Map<Object, Object> map) {
        return new Post(
            Long.parseLong((String) map.get("postId")),
            Long.parseLong((String) map.get("userId")),
            (String) map.get("content"),
            Instant.parse((String) map.get("createdAt"))
        );
    }
}
