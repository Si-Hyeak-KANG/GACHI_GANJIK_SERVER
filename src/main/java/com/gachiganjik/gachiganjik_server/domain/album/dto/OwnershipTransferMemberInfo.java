package com.gachiganjik.gachiganjik_server.domain.album.dto;

import com.gachiganjik.gachiganjik_server.domain.album.entity.AlbumMember;

public record OwnershipTransferMemberInfo(
        String memberId,
        String userId,
        String nickname,
        String role
) {
    public static OwnershipTransferMemberInfo of(AlbumMember member) {
        return new OwnershipTransferMemberInfo(
                String.valueOf(member.getMemberId()),
                member.getUserInfo() != null ? String.valueOf(member.getUserInfo().getUserId()) : null,
                member.getUserInfo() != null ? member.getUserInfo().getNickname() : null,
                member.getRole().name()
        );
    }
}
