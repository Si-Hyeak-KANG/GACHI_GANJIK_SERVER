package com.gachiganjik.gachiganjik_server.domain.album.dto;

import com.gachiganjik.gachiganjik_server.domain.album.entity.Album;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumMember;
import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumRole;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record AlbumDetailResponse(
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
        List<MemberInfo> members,
        String createdAt,
        String updatedAt
) {
    public record MemberInfo(
            String userId,
            String nickname,
            String userTag,
            String profileImageUrl,
            String role,
            String joinedAt
    ) {}

    public static AlbumDetailResponse of(Album album, AlbumRole myRole, List<AlbumMember> members) {
        List<String> categoryNames = album.getAlbumCategories().stream()
                .map(ac -> ac.getCategory().getCategoryName())
                .toList();

        List<MemberInfo> memberInfos = members.stream()
                .filter(m -> m.getUserInfo() != null)
                .map(m -> new MemberInfo(
                        String.valueOf(m.getUserInfo().getUserId()),
                        m.getUserInfo().getNickname(),
                        m.getUserInfo().getRandomId(),
                        m.getUserInfo().getProfileImageUrl(),
                        m.getRole().name(),
                        m.getJoinedDt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ))
                .toList();

        return new AlbumDetailResponse(
                String.valueOf(album.getAlbumId()),
                album.getTitle(),
                categoryNames,
                album.getEventStartDate() != null ? album.getEventStartDate().toString() : null,
                album.getEventEndDate() != null ? album.getEventEndDate().toString() : null,
                album.getCoverImageUrl(),
                album.getInviteCode(),
                myRole.name(),
                0,
                memberInfos.size(),
                memberInfos,
                album.getCreatedDt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                album.getUpdatedDt() != null ? album.getUpdatedDt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
        );
    }
}
