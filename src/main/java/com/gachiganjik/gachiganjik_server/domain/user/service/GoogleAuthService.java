package com.gachiganjik.gachiganjik_server.domain.user.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.user.dto.SocialUserProfile;
import com.gachiganjik.gachiganjik_server.domain.user.entity.LoginType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Google ID Token 을 검증하고 프로필만 반환한다.
 * 계정 조회/생성/토큰 발급은 SocialAuthService 가 담당한다.
 *
 * 미가입 사용자라도 토큰이 유효하면 정상적으로 프로필을 반환한다.
 * 여기서 예외를 던지는 경우는 토큰 자체가 유효하지 않을 때뿐이다.
 */
@Slf4j
@Service
public class GoogleAuthService {

    /** Android 발급 토큰은 스킴 없는 iss 를 쓰는 경우가 있어 두 형태를 모두 허용한다. */
    private static final List<String> GOOGLE_ISSUERS =
            List.of("accounts.google.com", "https://accounts.google.com");

    /**
     * 허용할 Google OAuth client ID 목록 (iOS / Android / Web).
     * 플랫폼마다 발급되는 idToken 의 aud 가 다르므로 단일 값으로는 검증할 수 없다.
     * 반드시 우리 Google Cloud 프로젝트가 소유한 client ID 만 넣는다.
     */
    @Value("${google.client-ids}")
    private List<String> googleClientIds;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    void initVerifier() {
        if (googleClientIds == null || googleClientIds.isEmpty()) {
            throw new IllegalStateException("google.client-ids 가 비어 있습니다.");
        }

        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(googleClientIds)
                .setIssuers(GOOGLE_ISSUERS)
                .build();

        log.info("Google ID Token verifier 초기화 완료. 허용 aud {}건", googleClientIds.size());
    }

    public SocialUserProfile fetchProfile(String idToken) {
        GoogleIdToken.Payload payload = verifyIdToken(idToken);

        return new SocialUserProfile(
                LoginType.GOOGLE,
                payload.getSubject(),
                payload.getEmail(),
                Boolean.TRUE.equals(payload.getEmailVerified()),
                (String) payload.get("name"),
                (String) payload.get("picture")
        );
    }

    private GoogleIdToken.Payload verifyIdToken(String idToken) {
        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                logVerificationFailure(idToken);
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }
            return googleIdToken.getPayload();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google ID Token 파싱 실패", e);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    /**
     * verify() 는 실패 사유를 알려주지 않고 null 만 반환한다.
     * 토큰을 다시 파싱해 어떤 조건이 어긋났는지 남긴다.
     * 서명 검증 전 값이므로 신뢰하지 않고 로깅에만 사용한다.
     */
    private void logVerificationFailure(String idToken) {
        try {
            GoogleIdToken.Payload payload =
                    GoogleIdToken.parse(GsonFactory.getDefaultInstance(), idToken).getPayload();

            log.warn("Google ID Token 검증 실패. aud={}, iss={}, exp={}, now={}, 허용 aud={}",
                    payload.getAudience(),
                    payload.getIssuer(),
                    payload.getExpirationTimeSeconds(),
                    System.currentTimeMillis() / 1000,
                    googleClientIds);
        } catch (Exception e) {
            log.warn("Google ID Token 검증 실패. 토큰 파싱 불가 (형식 오류 또는 손상)");
        }
    }
}