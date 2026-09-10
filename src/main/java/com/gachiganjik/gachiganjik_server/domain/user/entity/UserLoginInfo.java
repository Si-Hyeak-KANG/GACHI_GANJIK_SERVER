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
@Table(name = "TB_USER_LOGIN_INFO")
public class UserLoginInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_info_id")
    private Long loginInfoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo userInfo;

    @Column(name = "login_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Builder
    private UserLoginInfo(UserInfo userInfo, LoginType loginType, String email,
                          String passwordHash, String providerId) {
        this.userInfo = userInfo;
        this.loginType = loginType;
        this.email = email;
        this.passwordHash = passwordHash;
        this.providerId = providerId;
        this.status = UserStatus.ACTIVE;
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    /**
     * 회원 탈퇴 시 이메일을 익명화한다. UserInfo.email 만 비우면
     * AuthService#register / EmailVerificationService 의 중복 확인이 이 테이블의
     * email 을 그대로 조회하므로 재가입이 계속 막힌다. UNIQUE 제약이 없는 컬럼이라
     * null 처리에 충돌 위험은 없다(ADR-017).
     */
    public void anonymizeEmail() {
        this.email = null;
    }
}