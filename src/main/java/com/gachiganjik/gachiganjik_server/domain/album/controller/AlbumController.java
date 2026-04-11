package com.gachiganjik.gachiganjik_server.domain.album.controller;

import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.domain.album.dto.*;
import com.gachiganjik.gachiganjik_server.domain.album.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<AlbumSummaryResponse>>>> getMyAlbums(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(Map.of("albums", albumService.getMyAlbums(userId))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumCreateResponse>> createAlbum(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AlbumCreateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(albumService.createAlbum(userId, request)));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumDetailResponse>> getAlbumDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(albumService.getAlbumDetail(userId, albumId)));
    }

    @PatchMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumSummaryResponse>> updateAlbum(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @Valid @RequestBody AlbumUpdateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(albumService.updateAlbum(userId, albumId, request)));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<ApiResponse<?>> deleteAlbum(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        albumService.deleteAlbum(userId, albumId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<AlbumJoinResponse>> joinAlbum(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AlbumJoinRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(albumService.joinAlbum(userId, request)));
    }

    @GetMapping("/{albumId}/members")
    public ResponseEntity<ApiResponse<MemberListResponse>> getMembers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(albumService.getMembers(userId, albumId)));
    }
}