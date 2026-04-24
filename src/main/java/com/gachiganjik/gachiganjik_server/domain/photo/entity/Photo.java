package com.gachiganjik.gachiganjik_server.domain.photo.entity;

import com.gachiganjik.gachiganjik_server.common.auditing.BaseEntity;
import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_PHOTO")
public class Photo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id", nullable = false)
    private Moment moment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_user_id")
    private UserInfo uploaderUser;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "message", length = 100)
    private String message;

    @Column(name = "photo_date")
    private LocalDate photoDate;

    @Column(name = "color_code", length = 7)
    private String colorCode;

    @Column(name = "upload_dt", nullable = false)
    private LocalDateTime uploadDt;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PhotoStatus status;

    @Builder
    private Photo(Moment moment, Album album, UserInfo uploaderUser,
                  String imageUrl, String thumbnailUrl, String message,
                  LocalDate photoDate, String colorCode) {
        this.moment = moment;
        this.album = album;
        this.uploaderUser = uploaderUser;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.message = message;
        this.photoDate = photoDate;
        this.colorCode = colorCode;
        this.uploadDt = LocalDateTime.now();
        this.status = PhotoStatus.ACTIVE;
    }

    public void updateMessage(String message) {
        this.message = message;
    }

    public void delete() {
        this.status = PhotoStatus.DELETED;
    }
}