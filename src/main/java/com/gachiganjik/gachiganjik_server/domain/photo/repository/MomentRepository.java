package com.gachiganjik.gachiganjik_server.domain.photo.repository;

import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.Moment;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.MomentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MomentRepository extends JpaRepository<Moment, Long> {

    Optional<Moment> findByAlbumAndClientMomentId(Album album, String clientMomentId);

    Page<Moment> findByAlbumAndStatusOrderByMomentDateDesc(Album album, MomentStatus status, Pageable pageable);
}