package com.gachiganjik.gachiganjik_server.domain.user.dto;

/**
 * 소셜 로그인 요청.
 * Google 은 idToken(로컬 서명 검증), Kakao/Naver 는 accessToken(REST 조회)을 사용하므로
 * 두 필드를 공존시키고 provider 별로 필요한 값만 검증한다.
 */
public record SocialLoginRequest(
        String idToken,
        String accessToken
) {}