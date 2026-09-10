package com.gachiganjik.gachiganjik_server.domain.user.entity;

import com.gachiganjik.gachiganjik_server.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_USER_INFO")
public class UserInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "random_id", unique = true, length = 20)
    private String randomId;

    /** 프로필/알림용. 계정 매칭에는 사용하지 않는다. */
    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    /**
     * 계정 병합 키. 소유가 검증된 이메일만 저장한다.
     * 미검증이면 null 로 둔다. MySQL UNIQUE 는 NULL 중복을 허용하므로 충돌하지 않는다.
     */
    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Builder
    private UserInfo(String nickname, String profileImageUrl, String randomId,
                     String phone, String email) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.randomId = randomId;
        this.phone = phone;
        this.email = email;
        this.status = UserStatus.ACTIVE;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null) this.nickname = nickname;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    /**
     * 회원 탈퇴 처리. UNIQUE 제약(email/phone/randomId)은 유지하되 값을 비워
     * 동일 식별자로 재가입이 가능해지고, 탈퇴 계정으로의 소셜 재연결을 방지한다(ADR-017).
     */
    public void withdraw() {
        this.status = UserStatus.DELETED;
        this.email = null;
        this.phone = null;
        this.randomId = null;
    }
}