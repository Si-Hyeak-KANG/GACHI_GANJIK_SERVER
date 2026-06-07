package com.gachiganjik.gachiganjik_server.domain.comment.controller;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.common.security.GuestPrincipal;
import com.gachiganjik.gachiganjik_server.domain.comment.dto.CommentDto;
import com.gachiganjik.gachiganjik_server.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/albums/{albumId}/photos/{photoId}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<CommentDto.CommentListResponse>> getComments(
            Authentication authentication,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        if (isGuest(authentication)) {
            return ResponseEntity.ok(ApiResponse.success(
                    commentService.getCommentsAsGuest(extractGuestId(authentication), albumId, photoId, cursor, size)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getComments(extractUserId(authentication), albumId, photoId, cursor, size)));
    }

    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<CommentDto.CommentCreateResponse>> createComment(
            Authentication authentication,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @Valid @RequestBody CommentDto.CommentCreateRequest request) {
        if (isGuest(authentication)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            commentService.createCommentAsGuest(extractGuestId(authentication), albumId, photoId, request)));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        commentService.createComment(extractUserId(authentication), albumId, photoId, request)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            Authentication authentication,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @PathVariable Long commentId) {
        if (isGuest(authentication)) {
            // GUEST 댓글 삭제 미지원 (Comment에 guest_id FK 없음)
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        commentService.deleteComment(extractUserId(authentication), albumId, photoId, commentId);
        return ResponseEntity.ok(ApiResponse.<Void>success());
    }

    @PostMapping("/reactions")
    public ResponseEntity<ApiResponse<CommentDto.ReactionResponse>> toggleReaction(
            Authentication authentication,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @Valid @RequestBody CommentDto.ReactionRequest request) {
        if (isGuest(authentication)) {
            return ResponseEntity.ok(ApiResponse.success(
                    commentService.toggleReactionAsGuest(extractGuestId(authentication), albumId, photoId, request)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                commentService.toggleReaction(extractUserId(authentication), albumId, photoId, request)));
    }

    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

    private boolean isGuest(Authentication auth) {
        return auth.getPrincipal() instanceof GuestPrincipal;
    }

    private Long extractGuestId(Authentication auth) {
        return ((GuestPrincipal) auth.getPrincipal()).getGuestId();
    }

    private Long extractUserId(Authentication auth) {
        return Long.parseLong(((UserDetails) auth.getPrincipal()).getUsername());
    }
}