package com.test.minifeed.service;

import java.util.List;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.test.minifeed.model.Post;
import com.test.minifeed.model.User;

@Service
public class UserService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String POST_NEXT_ID_KEY = "post:nextId";
    private static final String POST_KEY_PREFIX = "post:";
    private static final String FOLLOWING_KEY_PREFIX = "following:";
    private static final String FOLLOWERS_KEY_PREFIX = "followers:";
    private static final String POSTS_KEY_PREFIX = "posts:";
    private static final String FEED_KEY_PREFIX = "feed:";
    private static final int MAX_FEED_SIZE = 200; 

    private static final String USER_NEXT_ID_KEY = "user:nextId";
    private static final String USER_KEY_PREFIX = "user:";
    private static final String ALL_USERS_SET = "all_users";

    public List<User> getAllUsers() {
        Set<Object> userIdObjects = redisTemplate.opsForSet().members(ALL_USERS_SET);

        if (userIdObjects == null || userIdObjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> users = new ArrayList<>();

        for (Object obj : userIdObjects) {
            try {
                Long userId = Long.parseLong(obj.toString());
                User user = getUserById(userId); 
                if (user != null) {
                    users.add(user);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return users;
    }

    private User getUserById(Long userId) {
        String userKey = USER_KEY_PREFIX + userId;
        Map<Object, Object> data = redisTemplate.opsForHash().entries(userKey);
        if (data.isEmpty()) return null;

        User user = new User();
        user.setUserId(userId);
        user.setUserName((String) data.get("username"));
        return user;
    }

    // Lấy danh sách ID những người mà user đang follow
    public List<Long> getFollowerIds(Long userId) {
        String followersKey = FOLLOWERS_KEY_PREFIX + userId;
        Set<Object> followerIds = redisTemplate.opsForSet().members(followersKey);

        if (followerIds == null || followerIds.isEmpty()) {
            return Collections.emptyList();
        }

        return followerIds.stream()
                .map(obj -> Long.parseLong((String) obj))
                .collect(Collectors.toList());
    }

    public User createUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        // Kiểm tra username đã tồn tại chưa (dùng Set để lưu unique usernames)
        String usernameKey = "username:" + username.toLowerCase();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(usernameKey))) {
            throw new IllegalArgumentException("Username already taken");
        }

        // Tạo user mới
        Long userId = redisTemplate.opsForValue().increment(USER_NEXT_ID_KEY);
        User user = new User();
        user.setUserId(userId);
        user.setUserName(username);

        // Lưu vào Redis 
        String userKey = USER_KEY_PREFIX + userId;
        Map<String, String> userData = new HashMap<>();
        userData.put("userId", userId.toString());
        userData.put("username", username);

        redisTemplate.opsForHash().putAll(userKey, userData);

        // Đánh dấu username đã dùng 
        redisTemplate.opsForValue().set(usernameKey, userId.toString());

        // Tạo các key rỗng cho follow/feed 
        redisTemplate.opsForSet().add(ALL_USERS_SET, userId.toString());
        return user;
    }

    public void follow(Long userId, Long targetUserId) {
        String followingKey = FOLLOWING_KEY_PREFIX + userId;
        String followersKey = FOLLOWERS_KEY_PREFIX + targetUserId;

        redisTemplate.opsForSet().add(followingKey, targetUserId.toString());
        redisTemplate.opsForSet().add(followersKey, userId.toString());
    }

    public void unfollow(Long userId, Long targetUserId) {
        String followingKey = FOLLOWING_KEY_PREFIX + userId;
        String followersKey = FOLLOWERS_KEY_PREFIX + targetUserId;

        redisTemplate.opsForSet().remove(followingKey, targetUserId.toString());
        redisTemplate.opsForSet().remove(followersKey, userId.toString());
    }

    public Post createPost(Long userId, String content) {
        //tạo dữ liệu post mới
        Long postId = redisTemplate.opsForValue().increment(POST_NEXT_ID_KEY);
        Instant createdAt = Instant.now();
        String postKey = POST_KEY_PREFIX + postId;
        Map<String, String> postData = new HashMap<>();
        postData.put("postId", postId.toString());
        postData.put("userId", userId.toString());
        postData.put("content", content);
        postData.put("createdAt", createdAt.toString());

        redisTemplate.opsForHash().putAll(postKey, postData);

        // Thêm post vào danh sách bài viết của user
        String postsKey = POSTS_KEY_PREFIX + userId;
        redisTemplate.opsForZSet().add(postsKey, postId.toString(), createdAt.toEpochMilli());

        // Thêm post vào feed của chính user
        String userFeedKey = FEED_KEY_PREFIX + userId;
        redisTemplate.opsForZSet().add(userFeedKey, postId.toString(), createdAt.toEpochMilli());
        trimFeed(userFeedKey);

        // Thêm post vào các feed của followers
        String followersKey = FOLLOWERS_KEY_PREFIX + userId;
        Set<Object> followers = redisTemplate.opsForSet().members(followersKey);
        if (followers != null) {
            for (Object followerObj : followers) {
                Long followerId = Long.parseLong((String) followerObj);
                String followerFeedKey = FEED_KEY_PREFIX + followerId;
                redisTemplate.opsForZSet().add(followerFeedKey, postId.toString(), createdAt.toEpochMilli());
                trimFeed(followerFeedKey);
            }
        }

        return new Post(postId, userId, content, createdAt);
    }

    private void trimFeed(String feedKey) {
        redisTemplate.opsForZSet().removeRange(feedKey, 0, -MAX_FEED_SIZE - 1);
    }
    
}
