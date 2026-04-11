package com.gachiganjik.gachiganjik_server.domain.user.dto;

import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;

import java.time.format.DateTimeFormatter;

public record UpdateProfileResponse(
        String userId,
        String nickname,
        String updatedAt
) {
    public static UpdateProfileResponse of(UserInfo userInfo) {
        return new UpdateProfileResponse(
                String.valueOf(userInfo.getUserId()),
                userInfo.getNickname(),
                userInfo.getUpdatedDt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}