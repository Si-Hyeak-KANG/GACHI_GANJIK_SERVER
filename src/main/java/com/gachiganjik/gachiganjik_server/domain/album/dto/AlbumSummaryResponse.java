package com.gachiganjik.gachiganjik_server.domain.album.dto;

import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumCategory;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumRole;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record AlbumSummaryResponse(
        String albumId,
        String title,
        List<String> categories,
        String eventStartDate,
        String eventEndDate,
        String coverImageUrl,
        String inviteCode,
        String role,
        int photoCount,
        int memberCount,
        String createdAt,
        String updatedAt
) {
    public static AlbumSummaryResponse of(Album album, AlbumRole role, int memberCount, int photoCount) {
        List<String> categoryNames = album.getAlbumCategories().stream()
                .map(ac -> ac.getCategory().getCategoryName())
                .toList();

        return new AlbumSummaryResponse(
                String.valueOf(album.getAlbumId()),
                album.getTitle(),
                categoryNames,
                album.getEventStartDate() != null ? album.getEventStartDate().toString() : null,
                album.getEventEndDate() != null ? album.getEventEndDate().toString() : null,
                album.getCoverImageUrl(),
                album.getInviteCode(),
                role.name(),
                photoCount,
                memberCount,
                album.getCreatedDt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                album.getUpdatedDt() != null ? album.getUpdatedDt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
        );
    }
}
