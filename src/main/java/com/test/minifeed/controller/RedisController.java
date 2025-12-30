package com.test.minifeed.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")  
public class RedisController {  

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Xóa tất cả dữ liệu trong Redis
    @DeleteMapping("/deleteAllData")
    public ResponseEntity<String> flushRedis() {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.serverCommands().flushAll();
                return null;
            }
        });
        return ResponseEntity.ok("All data in Redis has been cleared!");
    }
}