package com.gachiganjik.gachiganjik_server.domain.album.repository;

import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumMember;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumMemberStatus;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumRole;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AlbumMemberRepository extends JpaRepository<AlbumMember, Long> {
    Optional<AlbumMember> findByAlbumAndUserInfoAndStatus(Album album, UserInfo userInfo, AlbumMemberStatus status);
    boolean existsByAlbumAndUserInfoAndStatus(Album album, UserInfo userInfo, AlbumMemberStatus status);
    List<AlbumMember> findByAlbumAndStatus(Album album, AlbumMemberStatus status);

    @Query("SELECT am FROM AlbumMember am JOIN FETCH am.album a WHERE am.userInfo.userId = :userId AND am.status = 'ACTIVE'")
    List<AlbumMember> findActiveByUserId(Long userId);
}