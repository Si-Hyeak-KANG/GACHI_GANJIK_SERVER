package com.gachiganjik.gachiganjik_server.domain.guest.entity;

import com.gachiganjik.gachiganjik_server.common.auditing.BaseEntity;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_GUEST_INFO")
public class GuestInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Long guestId;

    @Column(name = "guest_key", unique = true, nullable = false, length = 20)
    private String guestKey;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private GuestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_user_id")
    private UserInfo linkedUser;

    @Builder
    private GuestInfo(String guestKey, String nickname) {
        this.guestKey = guestKey;
        this.nickname = nickname;
        this.status = GuestStatus.ACTIVE;
    }

    public void convert(UserInfo userInfo) {
        this.status = GuestStatus.CONVERTED;
        this.linkedUser = userInfo;
    }
}