package com.gachiganjik.gachiganjik_server.domain.album.dto;

import java.time.LocalDateTime;

public record AlbumEventPayload(
        String type,
        String albumId,
        String uploadedBy,
        int photoCount,
        LocalDateTime lastPhotoUploadedAt
) {
    public static AlbumEventPayload photoUploaded(String albumId, String uploaderNickname, int photoCount, LocalDateTime uploadedAt) {
        return new AlbumEventPayload(
                "PHOTO_UPLOADED",
                albumId,
                uploaderNickname,
                photoCount,
                uploadedAt
        );
    }
}