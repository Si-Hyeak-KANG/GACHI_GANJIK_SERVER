package com.gachiganjik.gachiganjik_server.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 이메일 회원가입 응답.
 * - 신규 이메일 : needsLink=false + 토큰 (계정 생성 완료)
 * - 기존 계정   : needsLink=true  + linkTicket + maskedEmail (연결 동의 대기, 토큰 없음)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SignupResponse(
        boolean needsLink,
        Long userId,
        String nickname,
        String accessToken,
        String refreshToken,
        String linkTicket,
        String maskedEmail
) {

    public static SignupResponse created(AuthResponse auth) {
        return new SignupResponse(false, auth.userId(), auth.nickname(),
                auth.accessToken(), auth.refreshToken(), null, null);
    }

    public static SignupResponse needsLink(String linkTicket, String maskedEmail) {
        return new SignupResponse(true, null, null, null, null, linkTicket, maskedEmail);
    }
}