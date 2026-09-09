package com.gachiganjik.gachiganjik_server.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 소셜 로그인 응답. 세 가지 분기를 갖는다.
 * - 기존 신원   : isNewUser=false, needsLink=false + 토큰
 * - 이메일 일치 : isNewUser=false, needsLink=true  + linkTicket + maskedEmail
 * - 신규        : isNewUser=true,  needsLink=false + signupTicket + profile
 *
 * isNewUser 와 needsLink 는 primitive 라 항상 응답에 포함된다. 나머지는 null 이면 생략된다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SocialLoginResponse(
        boolean isNewUser,
        boolean needsLink,
        Long userId,
        String nickname,
        String accessToken,
        String refreshToken,
        String signupTicket,
        Profile profile,
        String linkTicket,
        String maskedEmail
) {

    public record Profile(
            String nickname,
            String email,
            String profileImageUrl
    ) {}

    public static SocialLoginResponse existingUser(AuthResponse auth) {
        return new SocialLoginResponse(false, false,
                auth.userId(), auth.nickname(), auth.accessToken(), auth.refreshToken(),
                null, null, null, null);
    }

    public static SocialLoginResponse needsLink(String linkTicket, String maskedEmail) {
        return new SocialLoginResponse(false, true,
                null, null, null, null,
                null, null, linkTicket, maskedEmail);
    }

    public static SocialLoginResponse newUser(String signupTicket, SocialUserProfile profile) {
        return new SocialLoginResponse(true, false,
                null, null, null, null,
                signupTicket,
                new Profile(profile.nickname(), profile.email(), profile.profileImageUrl()),
                null, null);
    }
}