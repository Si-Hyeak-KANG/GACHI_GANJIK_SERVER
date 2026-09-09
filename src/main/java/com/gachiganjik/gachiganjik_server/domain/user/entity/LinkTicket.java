package com.gachiganjik.gachiganjik_server.domain.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * 계정 연결 확인 티켓.
 * 검증된 이메일로 기존 계정이 발견되면 발급되고, 사용자가 동의한 뒤
 * /auth/social/link 또는 /auth/link/email 에서 소비된 뒤 삭제된다.
 *
 * 연결 대상 userId 를 서버가 보관하므로 클라이언트가 대상 계정을 위조할 수 없다.
 * 비밀번호는 담지 않는다. EMAIL 연결 시 클라이언트가 링크 호출에 다시 실어 보낸다.
 */
@RedisHash("link_ticket")
public class LinkTicket {

    @Id
    private String ticketId;

    /** 붙일 로그인 수단. EMAIL 이면 /auth/link/email, 그 외는 /auth/social/link 로 처리한다. */
    private LoginType method;

    /** 소셜 수단일 때만 값이 있다. */
    private String providerUserId;

    private String email;

    private Long targetUserId;

    /** EMAIL 가입 도중 연결로 갈린 경우, 게스트 데이터 전환을 이어가기 위해 보관한다. */
    private String guestKey;

    @TimeToLive
    private long ttl;

    public LinkTicket(String ticketId, LoginType method, String providerUserId,
                      String email, Long targetUserId, String guestKey, long ttl) {
        this.ticketId = ticketId;
        this.method = method;
        this.providerUserId = providerUserId;
        this.email = email;
        this.targetUserId = targetUserId;
        this.guestKey = guestKey;
        this.ttl = ttl;
    }

    public String getTicketId() { return ticketId; }
    public LoginType getMethod() { return method; }
    public String getProviderUserId() { return providerUserId; }
    public String getEmail() { return email; }
    public Long getTargetUserId() { return targetUserId; }
    public String getGuestKey() { return guestKey; }
}