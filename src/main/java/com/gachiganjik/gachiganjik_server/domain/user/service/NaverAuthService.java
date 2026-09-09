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
 * Naver Access Token 으로 사용자 프로필만 조회한다.
 *
 * 네이버는 email_verified 에 해당하는 필드를 주지 않는다.
 * 다만 네이버 계정에 등록되는 이메일은 가입/변경 시 자체 인증을 거치므로,
 * 값이 존재하면 검증된 것으로 간주한다.
 */
@Slf4j
@Service
public class NaverAuthService {

    private static final String NAVER_USER_ME_URL = "https://openapi.naver.com/v1/nid/me";

    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient = RestClient.create();

    @SuppressWarnings("unchecked")
    public SocialUserProfile fetchProfile(String accessToken) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(NAVER_USER_ME_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(RESPONSE_TYPE);

            Map<String, Object> naverResponse = response != null
                    ? (Map<String, Object>) response.get("response")
                    : null;

            if (naverResponse == null || naverResponse.get("id") == null) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }

            String providerUserId  = (String) naverResponse.get("id");
            String email           = (String) naverResponse.get("email");
            String nickname        = (String) naverResponse.get("nickname");
            String name            = (String) naverResponse.get("name");
            String profileImageUrl = (String) naverResponse.get("profile_image");

            String resolvedNickname = (nickname != null && !nickname.isBlank()) ? nickname : name;
            boolean emailVerified = email != null && !email.isBlank();

            return new SocialUserProfile(
                    LoginType.NAVER, providerUserId, email, emailVerified,
                    resolvedNickname, profileImageUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Naver 사용자 정보 조회 실패", e);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}