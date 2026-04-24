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
@Table(name = "TB_MOMENT",
        uniqueConstraints = @UniqueConstraint(name = "uk_album_moment_date", columnNames = {"album_id", "moment_date"}))
public class Moment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moment_id")
    private Long momentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "moment_date", nullable = false)
    private LocalDate momentDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MomentStatus status;

    @Builder
    private Moment(Album album, LocalDate momentDate) {
        this.album = album;
        this.momentDate = momentDate;
        this.status = MomentStatus.ACTIVE;
    }
}