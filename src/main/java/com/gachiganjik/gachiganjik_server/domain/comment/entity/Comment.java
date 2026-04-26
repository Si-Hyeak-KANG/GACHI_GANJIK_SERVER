package com.gachiganjik.gachiganjik_server.domain.comment.entity;

import com.gachiganjik.gachiganjik_server.common.auditing.BaseEntity;
import com.gachiganjik.gachiganjik_server.domain.photo.entity.Photo;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_COMMENT",
        indexes = {
                @Index(name = "idx_comment_photo", columnList = "photo_id"),
                @Index(name = "idx_comment_user",  columnList = "user_id"),
                @Index(name = "idx_comment_created_dt", columnList = "created_dt")
        })
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    private Photo photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;

    @Column(name = "comment_text", nullable = false, length = 300)
    private String commentText;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    @Builder
    private Comment(Photo photo, UserInfo userInfo, String commentText) {
        this.photo = photo;
        this.userInfo = userInfo;
        this.commentText = commentText;
        this.status = CommentStatus.ACTIVE;
    }

    public void delete() {
        this.status = CommentStatus.DELETED;
    }
}