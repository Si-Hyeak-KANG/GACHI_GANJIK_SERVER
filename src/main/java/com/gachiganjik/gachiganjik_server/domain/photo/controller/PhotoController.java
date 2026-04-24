package com.gachiganjik.gachiganjik_server.domain.photo.controller;

import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.domain.photo.dto.PhotoDto;
import com.gachiganjik.gachiganjik_server.domain.photo.service.PhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/albums/{albumId}/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping
    public ResponseEntity<ApiResponse<PhotoDto.PhotoUploadResponse>> uploadPhotos(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @Valid @RequestBody PhotoDto.PhotoUploadRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(photoService.uploadPhotos(userId, albumId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PhotoDto.PhotoListResponse>> getPhotos(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(photoService.getPhotos(userId, albumId, page, size)));
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<ApiResponse<PhotoDto.PhotoDetailResponse>> getPhotoDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long photoId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(photoService.getPhotoDetail(userId, albumId, photoId)));
    }

    @PatchMapping("/{photoId}")
    public ResponseEntity<ApiResponse<PhotoDto.PhotoMessageUpdateResponse>> updateMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @Valid @RequestBody PhotoDto.PhotoMessageUpdateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(photoService.updateMessage(userId, albumId, photoId, request)));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long photoId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        photoService.deletePhoto(userId, albumId, photoId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{photoId}/download")
    public ResponseEntity<ApiResponse<PhotoDto.PhotoDownloadResponse>> getDownloadUrl(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long albumId,
            @PathVariable Long photoId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(photoService.getDownloadUrl(userId, albumId, photoId)));
    }
}