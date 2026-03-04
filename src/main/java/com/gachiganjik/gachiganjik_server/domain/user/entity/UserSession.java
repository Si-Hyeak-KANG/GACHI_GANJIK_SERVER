package com.gachiganjik.gachiganjik_server.domain.user.entity;

import com.gachiganjik.gachiganjik_server.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_USER_SESSION")
public class UserSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo userInfo;

    @Column(name = "session_token", nullable = false, unique = true, length = 255)
    private String sessionToken;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "login_dt", nullable = false)
    private LocalDateTime loginDt;

    @Column(name = "logout_dt")
    private LocalDateTime logoutDt;

    @Column(name = "expire_dt", nullable = false)
    private LocalDateTime expireDt;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserSessionStatus status;

    @Builder
    private UserSession(UserInfo userInfo, String sessionToken,
                        String deviceInfo, String ipAddress, LocalDateTime expireDt) {
        this.userInfo = userInfo;
        this.sessionToken = sessionToken;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.loginDt = LocalDateTime.now();
        this.expireDt = expireDt;
        this.status = UserSessionStatus.ACTIVE;
    }

    public void logout() {
        this.logoutDt = LocalDateTime.now();
        this.status = UserSessionStatus.LOGGED_OUT;
    }
}