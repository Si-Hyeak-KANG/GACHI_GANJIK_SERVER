package com.gachiganjik.gachiganjik_server.domain.photo.dto;

import com.gachiganjik.gachiganjik_server.domain.photo.entity.Photo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class PhotoDto {

    // ──────────────────────────────────────────
    // Request
    // ──────────────────────────────────────────

    public record PhotoUploadItem(
            @NotEmpty String imageUrl,
            String thumbnailUrl,
            String message,
            String colorCode
    ) {}

    public record PhotoUploadRequest(
            @NotEmpty @Size(max = 10) List<PhotoUploadItem> photos,
            String photoDate,       // yyyy-MM-dd, null 허용
            String momentId,        // client-generated UUID v4
            String message          // 업로드 배치 공통 메시지
    ) {}

    public record PhotoMessageUpdateRequest(
            @Size(max = 100) String message
    ) {}

    // ──────────────────────────────────────────
    // Response
    // ──────────────────────────────────────────

    public record PhotoSummary(
            String photoId,
            String momentId,
            String imageUrl,
            String thumbnailUrl,
            String uploaderId,
            String uploaderNickname,
            String uploaderProfileImageUrl,
            String message,
            String photoDate,
            String colorCode
    ) {
        public static PhotoSummary of(Photo photo) {
            String uploaderId = photo.getUploaderUser() != null
                    ? String.valueOf(photo.getUploaderUser().getUserId())
                    : null;
            String uploaderNickname = photo.getUploaderUser() != null
                    ? photo.getUploaderUser().getNickname()
                    : null;
            String uploaderProfileImageUrl = photo.getUploaderUser() != null
                    ? photo.getUploaderUser().getProfileImageUrl()
                    : null;
            return new PhotoSummary(
                    String.valueOf(photo.getPhotoId()),
                    photo.getMoment().getClientMomentId(),
                    photo.getImageUrl(),
                    photo.getThumbnailUrl(),
                    uploaderId,
                    uploaderNickname,
                    uploaderProfileImageUrl,
                    photo.getMessage(),
                    photo.getPhotoDate() != null ? photo.getPhotoDate().toString() : null,
                    photo.getColorCode()
            );
        }
    }

    public record MomentResponse(
            String momentId,
            String date,
            String comment,
            List<PhotoSummary> photos
    ) {}

    public record PhotoListResponse(
            List<MomentResponse> moments,
            boolean hasNext
    ) {}

    public record PhotoUploadResponse(
            List<PhotoSummary> photos
    ) {}

    public record PhotoDetailResponse(
            String photoId,
            String albumId,
            String imageUrl,
            String thumbnailUrl,
            String uploaderId,
            String uploaderNickname,
            String uploaderProfileImageUrl,
            String message,
            String photoDate,
            String colorCode,
            String uploadDt
    ) {
        public static PhotoDetailResponse of(Photo photo) {
            String uploaderId = photo.getUploaderUser() != null
                    ? String.valueOf(photo.getUploaderUser().getUserId())
                    : null;
            String uploaderNickname = photo.getUploaderUser() != null
                    ? photo.getUploaderUser().getNickname()
                    : null;
            String uploaderProfileImageUrl = photo.getUploaderUser() != null
                    ? photo.getUploaderUser().getProfileImageUrl()
                    : null;
            return new PhotoDetailResponse(
                    String.valueOf(photo.getPhotoId()),
                    String.valueOf(photo.getAlbum().getAlbumId()),
                    photo.getImageUrl(),
                    photo.getThumbnailUrl(),
                    uploaderId,
                    uploaderNickname,
                    uploaderProfileImageUrl,
                    photo.getMessage(),
                    photo.getPhotoDate() != null ? photo.getPhotoDate().toString() : null,
                    photo.getColorCode(),
                    photo.getUploadDt().toString()
            );
        }
    }

    public record PhotoMessageUpdateResponse(
            String photoId,
            String message,
            String updatedAt
    ) {
        public static PhotoMessageUpdateResponse of(Photo photo) {
            return new PhotoMessageUpdateResponse(
                    String.valueOf(photo.getPhotoId()),
                    photo.getMessage(),
                    photo.getUpdatedDt() != null ? photo.getUpdatedDt().toString() : null
            );
        }
    }

    public record PhotoDownloadResponse(
            String photoId,
            String imageUrl
    ) {
        public static PhotoDownloadResponse of(Photo photo) {
            return new PhotoDownloadResponse(
                    String.valueOf(photo.getPhotoId()),
                    photo.getImageUrl()
            );
        }
    }
}