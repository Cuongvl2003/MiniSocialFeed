package com.test.minifeed.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.test.minifeed.model.Post;
import com.test.minifeed.model.User;
import com.test.minifeed.model.Request.CreateUserRequest;
import com.test.minifeed.model.Request.PostRequest;
import com.test.minifeed.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController  
@RequestMapping()
public class UserController {
    @Autowired
    private UserService userService;

    // Lây danh sách tất cả người dùng
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Lây danh sách người dùng mà user đang follow
    @GetMapping("/users/{userId}/following")
    public ResponseEntity<List<Long>> getFollowing(@PathVariable Long userId) {
        List<Long> following = userService.getFollowerIds(userId);
        return ResponseEntity.ok(following);
    }

    // Tạo người dùng mới
    @PostMapping("/createUsers")
        public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.getUsername());
        return ResponseEntity.ok(user);
    }
    
    // Follow một người dùng
    @PostMapping("/users/{userId}/follow/{targetUserId}")
    public ResponseEntity<Void> follow(@PathVariable Long userId, @PathVariable Long targetUserId) {
        userService.follow(userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    // Unfollow một người dùng
    @PostMapping("/users/{userId}/unfollow/{targetUserId}")
    public ResponseEntity<Void> unfollow(@PathVariable Long userId, @PathVariable Long targetUserId) {
        userService.unfollow(userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    // Tạo post mới
    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody PostRequest postRequest) {
        Post post = userService.createPost(postRequest.getUserId(), postRequest.getContent());
        return ResponseEntity.ok(post);
    }

}
