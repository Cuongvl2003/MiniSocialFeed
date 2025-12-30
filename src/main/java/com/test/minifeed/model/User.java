package com.test.minifeed.model;

import org.springframework.data.annotation.Id;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class User {
    @Id
    private long userId;
    private String userName;
}
