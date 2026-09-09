package com.gachiganjik.gachiganjik_server.domain.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * 소셜 신규 가입 온보딩 티켓.
 * /auth/social/{provider} 에서 신규 유저로 판별되면 발급되고,
 * /auth/social/complete 에서 소비된 뒤 삭제된다.
 *
 * emailVerified 는 제공자가 이메일 소유를 검증했는지 여부다.
 * 계정 생성 시 이 값이 true 일 때만 TB_USER_INFO.email(병합 키)에 기록한다.
 */
@RedisHash("signup_ticket")
public class SignupTicket {

    @Id
    private String ticketId;

    /** EMAIL 은 들어올 수 없다. SocialAuthService 에서 사전 차단한다. */
    private LoginType provider;

    private String providerUserId;

    private String nickname;

    private String email;

    private boolean emailVerified;

    private String profileImageUrl;

    @TimeToLive
    private long ttl;

    public SignupTicket(String ticketId, LoginType provider, String providerUserId,
                        String nickname, String email, boolean emailVerified,
                        String profileImageUrl, long ttl) {
        this.ticketId = ticketId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.nickname = nickname;
        this.email = email;
        this.emailVerified = emailVerified;
        this.profileImageUrl = profileImageUrl;
        this.ttl = ttl;
    }

    public String getTicketId() { return ticketId; }
    public LoginType getProvider() { return provider; }
    public String getProviderUserId() { return providerUserId; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public boolean isEmailVerified() { return emailVerified; }
    public String getProfileImageUrl() { return profileImageUrl; }
}