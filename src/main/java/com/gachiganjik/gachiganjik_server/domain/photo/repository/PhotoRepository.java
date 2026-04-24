package com.gachiganjik.gachiganjik_server.domain.photo.repository;

import com.gachiganjik.gachiganjik_server.domain.photo.entity.Moment;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.Photo;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.PhotoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByMomentAndStatusOrderByUploadDtAsc(Moment moment, PhotoStatus status);

    Optional<Photo> findByPhotoIdAndStatus(Long photoId, PhotoStatus status);

    @Query("SELECT COUNT(p) FROM Photo p WHERE p.album.albumId = :albumId AND p.status = :status")
    int countByAlbumAndStatus(@Param("albumId") Long albumId, @Param("status") PhotoStatus status);
}