package com.gachiganjik.gachiganjik_server.domain.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("email_verification")
public class EmailVerificationCode {

    @Id
    private String email;

    private String code;

    @TimeToLive
    private long ttl;

    public EmailVerificationCode(String email, String code, long ttl) {
        this.email = email;
        this.code = code;
        this.ttl = ttl;
    }

    public String getEmail() { return email; }
    public String getCode() { return code; }
}