package com.project.myacademy.global.configuration.refreshToken;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Id;

@AllArgsConstructor
@Getter
@RedisHash(value = "jwtToken", timeToLive = 60)
public class RefreshToken {

    @Id
    private String id;

    private String refreshToken;

    @Indexed
    private String accessToken;

}