package com.gachiganjik.gachiganjik_server.domain.photo.controller;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.common.security.GuestPrincipal;
import com.gachiganjik.gachiganjik_server.domain.photo.dto.PhotoDto;
import com.gachiganjik.gachiganjik_server.domain.photo.service.PhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/albums/{albumId}/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping
    public ResponseEntity<ApiResponse<PhotoDto.PhotoUploadResponse>> uploadPhotos(
            Authentication authentication,
            @PathVariable Long albumId,
            @Valid @RequestBody PhotoDto.PhotoUploadRequest request) {
        if (isGuest(authentication)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            photoService.uploadPhotosAsGuest(extractGuestId(authentication), albumId, request)));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        photoService.uploadPhotos(extractUserId(authentication), albumId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PhotoDto.PhotoListResponse>> getPhotos(
            Authentication authentication,
            @PathVariable Long albumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (isGuest(authentication)) {
            return ResponseEntity.ok(ApiResponse.success(
                    photoService.getPhotosAsGuest(extractGuestId(authentication), albumId, page, size)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                photoService.getPhotos(extractUserId(authentication), albumId, page, size)));
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<ApiResponse<PhotoDto.PhotoDetailResponse>> getPhotoDetail(
            Authentication authentication,
            @PathVariable Long albumId,
            @PathVariable Long photoId) {
        if (isGuest(authentication)) {
            return ResponseEntity.ok(ApiResponse.success(
                    photoService.getPhotoDetailAsGuest(extractGuestId(authentication), albumId, photoId)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                photoService.getPhotoDetail(extractUserId(authentication), albumId, photoId)));
    }

    @PatchMapping("/{photoId}")
    public ResponseEntity<ApiResponse<PhotoDto.PhotoMessageUpdateResponse>> updateMessage(
            Authentication authentication,
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @Valid @RequestBody PhotoDto.PhotoMessageUpdateRequest request) {
        if (isGuest(authentication)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        return ResponseEntity.ok(ApiResponse.success(
                photoService.updateMessage(extractUserId(authentication), albumId, photoId, request)));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            Authentication authentication,
            @PathVariable Long albumId,
            @PathVariable Long photoId) {
        if (isGuest(authentication)) {
            photoService.deletePhotoAsGuest(extractGuestId(authentication), albumId, photoId);
        } else {
            photoService.deletePhoto(extractUserId(authentication), albumId, photoId);
        }
        return ResponseEntity.ok(ApiResponse.<Void>success());
    }

    @GetMapping("/{photoId}/download")
    public ResponseEntity<ApiResponse<PhotoDto.PhotoDownloadResponse>> getDownloadUrl(
            Authentication authentication,
            @PathVariable Long albumId,
            @PathVariable Long photoId) {
        if (isGuest(authentication)) {
            return ResponseEntity.ok(ApiResponse.success(
                    photoService.getDownloadUrlAsGuest(extractGuestId(authentication), albumId, photoId)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                photoService.getDownloadUrl(extractUserId(authentication), albumId, photoId)));
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