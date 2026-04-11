package com.gachiganjik.gachiganjik_server.domain.album.entity;

import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_ALBUM_MEMBER",
        uniqueConstraints = @UniqueConstraint(name = "uk_album_user", columnNames = {"album_id", "user_id"}))
public class AlbumMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;

    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AlbumRole role;

    @Column(name = "joined_dt", nullable = false)
    private LocalDateTime joinedDt;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AlbumMemberStatus status;

    @Builder
    private AlbumMember(Album album, UserInfo userInfo, AlbumRole role) {
        this.album = album;
        this.userInfo = userInfo;
        this.role = role;
        this.joinedDt = LocalDateTime.now();
        this.status = AlbumMemberStatus.ACTIVE;
    }

    public void updateRole(AlbumRole role) {
        this.role = role;
    }

    public void leave() {
        this.status = AlbumMemberStatus.LEFT;
    }
}