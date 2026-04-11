package com.gachiganjik.gachiganjik_server.domain.album.entity;

import com.gachiganjik.gachiganjik_server.common.auditing.BaseEntity;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_ALBUM")
public class Album extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "event_start_date")
    private LocalDate eventStartDate;

    @Column(name = "event_end_date")
    private LocalDate eventEndDate;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "invite_code", nullable = false, unique = true, length = 10)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserInfo ownerUser;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AlbumStatus status;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlbumCategory> albumCategories = new ArrayList<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlbumMember> albumMembers = new ArrayList<>();

    @Builder
    private Album(String title, LocalDate eventStartDate, LocalDate eventEndDate,
                  String coverImageUrl, String inviteCode, UserInfo ownerUser) {
        this.title = title;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.coverImageUrl = coverImageUrl;
        this.inviteCode = inviteCode;
        this.ownerUser = ownerUser;
        this.status = AlbumStatus.ACTIVE;
    }

    public void update(String title, LocalDate eventStartDate, LocalDate eventEndDate, String coverImageUrl) {
        if (title != null) this.title = title;
        if (eventStartDate != null) this.eventStartDate = eventStartDate;
        if (eventEndDate != null) this.eventEndDate = eventEndDate;
        if (coverImageUrl != null) this.coverImageUrl = coverImageUrl;
    }

    public void delete() {
        this.status = AlbumStatus.DELETED;
    }
}