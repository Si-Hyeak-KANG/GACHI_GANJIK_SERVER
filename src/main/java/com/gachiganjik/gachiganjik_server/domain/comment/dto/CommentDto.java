package com.gachiganjik.gachiganjik_server.domain.comment.dto;

import com.gachiganjik.gachiganjik_server.domain.comment.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CommentDto {

    // ──────────────────────────────────────────
    // Request
    // ──────────────────────────────────────────

    public record CommentCreateRequest(
            @NotBlank @Size(min = 1, max = 300) String content
    ) {}

    public record ReactionRequest(
            @NotBlank String reactionType
    ) {}

    // ──────────────────────────────────────────
    // Response
    // ──────────────────────────────────────────

    public record CommentInfo(
            String commentId,
            String authorId,
            String nickname,
            String profileImageUrl,
            String content,
            String createdAt,
            boolean isMine
    ) {
        public static CommentInfo of(Comment comment, Long currentUserId) {
            String authorId = comment.getUserInfo() != null
                    ? String.valueOf(comment.getUserInfo().getUserId())
                    : null;
            String nickname = comment.getUserInfo() != null
                    ? comment.getUserInfo().getNickname()
                    : null;
            String profileImageUrl = comment.getUserInfo() != null
                    ? comment.getUserInfo().getProfileImageUrl()
                    : null;
            boolean isMine = comment.getUserInfo() != null
                    && comment.getUserInfo().getUserId().equals(currentUserId);

            return new CommentInfo(
                    String.valueOf(comment.getCommentId()),
                    authorId,
                    nickname,
                    profileImageUrl,
                    comment.getCommentText(),
                    comment.getCreatedDt().toString(),
                    isMine
            );
        }
    }

    public record CommentListResponse(
            List<CommentInfo> comments,
            String nextCursor,
            boolean hasNext
    ) {}

    public record CommentCreateResponse(
            String commentId,
            String content,
            String createdAt
    ) {
        public static CommentCreateResponse of(Comment comment) {
            return new CommentCreateResponse(
                    String.valueOf(comment.getCommentId()),
                    comment.getCommentText(),
                    comment.getCreatedDt().toString()
            );
        }
    }

    public record ReactionResponse(
            boolean isLiked,
            int likeCount
    ) {}
}