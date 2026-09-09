package com.gachiganjik.gachiganjik_server.domain.user.dto;

import com.gachiganjik.gachiganjik_server.domain.user.entity.LoginType;

/**
 * 각 소셜 제공자로부터 조회한 프로필. 서버 내부 전달용이며 응답으로 직접 나가지 않는다.
 * emailVerified 가 true 일 때만 계정 병합에 사용한다.
 */
public record SocialUserProfile(
        LoginType provider,
        String providerUserId,
        String email,
        boolean emailVerified,
        String nickname,
        String profileImageUrl
) {}