package com.gachiganjik.gachiganjik_server.domain.comment.controller;

import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.domain.comment.dto.CommentDto;
import com.gachiganjik.gachiganjik_server.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/albums/{albumId}/photos/{photoId}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<CommentDto.CommentListResponse>> getComments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getComments(userId, albumId, photoId, cursor, size)));
    }

    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<CommentDto.CommentCreateResponse>> createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @Valid @RequestBody CommentDto.CommentCreateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        commentService.createComment(userId, albumId, photoId, request)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @PathVariable Long commentId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.deleteComment(userId, albumId, photoId, commentId);
        return ResponseEntity.ok(ApiResponse.<Void>success());
    }

    @PostMapping("/reactions")
    public ResponseEntity<ApiResponse<CommentDto.ReactionResponse>> toggleReaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @Valid @RequestBody CommentDto.ReactionRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                commentService.toggleReaction(userId, albumId, photoId, request)));
    }
}