package com.test.minifeed.model.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequest {
    private Long userId;
    private String content;
}
