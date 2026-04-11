package com.gachiganjik.gachiganjik_server.domain.user.dto;

import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserLoginInfo;

import java.time.format.DateTimeFormatter;

public record UserProfileResponse(
        String userId,
        String email,
        String nickname,
        String userTag,
        String profileImageUrl,
        int albumCount,
        String createdAt
) {
    public static UserProfileResponse of(UserInfo userInfo, UserLoginInfo loginInfo) {
        return new UserProfileResponse(
                String.valueOf(userInfo.getUserId()),
                loginInfo.getEmail(),
                userInfo.getNickname(),
                userInfo.getRandomId(),
                userInfo.getProfileImageUrl(),
                0,
                userInfo.getCreatedDt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}