package com.gachiganjik.gachiganjik_server.domain.photo.entity;

import com.gachiganjik.gachiganjik_server.common.auditing.BaseEntity;
import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_MOMENT")
public class Moment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moment_id")
    private Long momentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "client_moment_id", nullable = false, length = 36)
    private String clientMomentId;

    @Column(name = "moment_date", nullable = false)
    private LocalDate momentDate;

    @Column(name = "message", length = 100)
    private String message;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MomentStatus status;

    @Builder
    private Moment(Album album, String clientMomentId, LocalDate momentDate, String message) {
        this.album = album;
        this.clientMomentId = clientMomentId;
        this.momentDate = momentDate;
        this.message = message;
        this.status = MomentStatus.ACTIVE;
    }
}