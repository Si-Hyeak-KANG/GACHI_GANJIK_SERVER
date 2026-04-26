package com.gachiganjik.gachiganjik_server.domain.comment.repository;

import com.gachiganjik.gachiganjik_server.domain.comment.entity.PhotoReaction;
import com.gachiganjik.gachiganjik_server.domain.comment.entity.ReactionType;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.Photo;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PhotoReactionRepository extends JpaRepository<PhotoReaction, Long> {

    Optional<PhotoReaction> findByPhotoAndUserInfoAndReactionType(
            Photo photo, UserInfo userInfo, ReactionType reactionType);

    @Query("SELECT COUNT(r) FROM PhotoReaction r WHERE r.photo.photoId = :photoId AND r.reactionType = :reactionType")
    int countByPhotoIdAndReactionType(@Param("photoId") Long photoId, @Param("reactionType") ReactionType reactionType);

    boolean existsByPhotoPhotoIdAndUserInfoUserIdAndReactionType(
            Long photoId, Long userId, ReactionType reactionType);
}