package com.test.minifeed.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.test.minifeed.model.Post;

@Service
public class FeedService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String POST_KEY_PREFIX = "post:";
    private static final String FEED_KEY_PREFIX = "feed:";

    public List<Post> getFeed(Long userId) {
        String feedKey = FEED_KEY_PREFIX + userId;

        // Lấy 20 bài viết mới nhất từ feed của user
        Set<Object> postIdObjects = redisTemplate.opsForZSet().reverseRange(feedKey, 0, 19);

        if (postIdObjects == null || postIdObjects.isEmpty()) {
            return Collections.emptyList();
        }

        return postIdObjects.stream()
            .map(obj -> (String) obj)
            .map(postIdStr -> {
                String postKey = POST_KEY_PREFIX + postIdStr;
                Map<Object, Object> postData = redisTemplate.opsForHash().entries(postKey);
                    return Post.fromMap(postData);
            })
            .collect(Collectors.toList()); 
    }

}
