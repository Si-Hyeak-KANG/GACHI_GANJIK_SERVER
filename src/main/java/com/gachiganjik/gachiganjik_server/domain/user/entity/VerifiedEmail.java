package com.gachiganjik.gachiganjik_server.domain.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * 이메일 인증 완료 마커.
 * /auth/email/verify 성공 시 저장되고, /auth/signup 에서 소비된 뒤 삭제된다.
 *
 * 이 마커가 없으면 회원가입을 거부한다. 없으면 /auth/email/verify 를 건너뛴
 * 직접 호출로 임의의 이메일 계정을 만들 수 있고, 이메일이 계정 병합 키이므로
 * 그대로 계정 탈취 경로가 된다.
 */
@RedisHash("verified_email")
public class VerifiedEmail {

    @Id
    private String email;

    @TimeToLive
    private long ttl;

    public VerifiedEmail(String email, long ttl) {
        this.email = email;
        this.ttl = ttl;
    }

    public String getEmail() {
        return email;
    }
}