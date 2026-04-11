package com.gachiganjik.gachiganjik_server.domain.album.repository;

import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    boolean existsByInviteCode(String inviteCode);
    Optional<Album> findByInviteCodeAndStatus(String inviteCode, AlbumStatus status);

    @Query("SELECT COUNT(am) FROM AlbumMember am WHERE am.userInfo.userId = :userId AND am.status = 'ACTIVE'")
    long countActiveAlbumsByUserId(Long userId);
}