package com.gachiganjik.gachiganjik_server.domain.comment.repository;

import com.gachiganjik.gachiganjik_server.domain.comment.entity.Comment;
import com.gachiganjik.gachiganjik_server.domain.comment.entity.CommentStatus;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.Photo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.photo = :photo AND c.status = :status " +
            "AND (:cursor IS NULL OR c.commentId > :cursor) ORDER BY c.commentId ASC")
    List<Comment> findByPhotoAndStatusWithCursor(
            @Param("photo") Photo photo,
            @Param("status") CommentStatus status,
            @Param("cursor") Long cursor,
            Pageable pageable);

    Optional<Comment> findByCommentIdAndStatus(Long commentId, CommentStatus status);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.photo.photoId = :photoId AND c.status = :status")
    int countByPhotoIdAndStatus(@Param("photoId") Long photoId, @Param("status") CommentStatus status);
}