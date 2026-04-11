package com.gachiganjik.gachiganjik_server.domain.album.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_ALBUM_CATEGORY",
        uniqueConstraints = @UniqueConstraint(name = "uk_album_category", columnNames = {"album_id", "category_id"}))
public class AlbumCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_category_id")
    private Long albumCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Builder
    private AlbumCategory(Album album, Category category) {
        this.album = album;
        this.category = category;
    }
}