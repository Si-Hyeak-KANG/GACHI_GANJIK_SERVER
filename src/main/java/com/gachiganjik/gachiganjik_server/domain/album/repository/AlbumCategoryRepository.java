package com.gachiganjik.gachiganjik_server.domain.album.repository;

import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumCategoryRepository extends JpaRepository<AlbumCategory, Long> {
    List<AlbumCategory> findByAlbum(Album album);
    void deleteByAlbum(Album album);
}