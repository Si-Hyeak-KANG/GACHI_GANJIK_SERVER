package com.gachiganjik.gachiganjik_server.domain.album.dto;

import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumMember;

public record MemberRoleUpdateResponse(
        String memberId,
        String userId,
        String nickname,
        String role
) {
    public static MemberRoleUpdateResponse of(AlbumMember member) {
        return new MemberRoleUpdateResponse(
                String.valueOf(member.getMemberId()),
                member.getUserInfo() != null ? String.valueOf(member.getUserInfo().getUserId()) : null,
                member.getUserInfo() != null ? member.getUserInfo().getNickname() : null,
                member.getRole().name()
        );
    }
}