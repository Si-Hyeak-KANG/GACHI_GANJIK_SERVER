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
@Table(name = "TB_PHOTO_REACTION",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_photo_user_reaction",
                columnNames = {"photo_id", "user_id", "reaction_type"}
        ),
        indexes = {
                @Index(name = "idx_reaction_photo_user", columnList = "photo_id, user_id"),
                @Index(name = "idx_reaction_user",       columnList = "user_id")
        })
public class PhotoReaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Long reactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    private Photo photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;

    @Column(name = "reaction_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;

    @Builder
    private PhotoReaction(Photo photo, UserInfo userInfo, ReactionType reactionType) {
        this.photo = photo;
        this.userInfo = userInfo;
        this.reactionType = reactionType;
    }
}