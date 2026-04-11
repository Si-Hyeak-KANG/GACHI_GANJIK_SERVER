package com.gachiganjik.gachiganjik_server.domain.album.dto;

import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumMember;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record MemberListResponse(List<MemberInfo> members) {

    public record MemberInfo(
            String userId,
            String nickname,
            String userTag,
            String profileImageUrl,
            String role,
            String joinedAt
    ) {}

    public static MemberListResponse of(List<AlbumMember> members) {
        List<MemberInfo> infos = members.stream()
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
        return new MemberListResponse(infos);
    }
}
