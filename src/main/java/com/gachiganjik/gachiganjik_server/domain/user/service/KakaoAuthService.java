package com.gachiganjik.gachiganjik_server.domain.user.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.user.dto.SocialUserProfile;
import com.gachiganjik.gachiganjik_server.domain.user.entity.LoginType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Kakao Access Token 으로 사용자 프로필만 조회한다.
 * 이메일 제공이 선택 동의라 email 이 null 일 수 있고, 동의해도 미인증 상태일 수 있다.
 */
@Slf4j
@Service
public class KakaoAuthService {

    private static final String KAKAO_USER_ME_URL = "https://kapi.kakao.com/v2/user/me";

    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient = RestClient.create();

    @SuppressWarnings("unchecked")
    public SocialUserProfile fetchProfile(String accessToken) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(KAKAO_USER_ME_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(RESPONSE_TYPE);

            if (response == null || response.get("id") == null) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }

            String providerUserId = String.valueOf(response.get("id"));

            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            boolean emailVerified = email != null && kakaoAccount != null
                    && Boolean.TRUE.equals(kakaoAccount.get("is_email_verified"));

            Map<String, Object> profile = kakaoAccount != null
                    ? (Map<String, Object>) kakaoAccount.get("profile")
                    : null;
            String nickname = profile != null ? (String) profile.get("nickname") : null;
            String profileImageUrl = profile != null ? (String) profile.get("profile_image_url") : null;

            return new SocialUserProfile(
                    LoginType.KAKAO, providerUserId, email, emailVerified, nickname, profileImageUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kakao 사용자 정보 조회 실패", e);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}