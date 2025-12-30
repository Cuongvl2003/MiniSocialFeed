package com.test.minifeed.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.test.minifeed.model.Post;
import com.test.minifeed.service.FeedService;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController  
@RequestMapping
public class FeedController {
    @Autowired
    private FeedService feedService;

    // Lấy feed cho một người dùng
    @GetMapping("/users/{userId}/feed")
    public ResponseEntity<List<Post>> getFeed(@PathVariable Long userId) {
        List<Post> feed = feedService.getFeed(userId);
        return ResponseEntity.ok(feed);
    }
}
