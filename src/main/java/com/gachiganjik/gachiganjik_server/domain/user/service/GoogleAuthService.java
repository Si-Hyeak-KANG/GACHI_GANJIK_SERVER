package com.gachiganjik.gachiganjik_server.domain.user.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.user.dto.AuthResponse;
import com.gachiganjik.gachiganjik_server.domain.user.entity.LoginType;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserLoginInfo;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserInfoRepository;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserLoginInfoRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleAuthService {

    private final UserInfoRepository userInfoRepository;
    private final UserLoginInfoRepository userLoginInfoRepository;
    private final AuthService authService;

    @Value("${google.client-id}")
    private String googleClientId;

    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        GoogleIdToken.Payload payload = verifyIdToken(idToken);

        String providerId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        UserLoginInfo loginInfo = userLoginInfoRepository
                .findByEmailAndLoginType(email, LoginType.GOOGLE)
                .orElseGet(() -> createGoogleUser(email, name, providerId));

        return authService.issueTokensAsAuthResponse(loginInfo.getUserInfo());
    }

    private GoogleIdToken.Payload verifyIdToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }
            return googleIdToken.getPayload();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google ID Token 검증 실패", e);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private UserLoginInfo createGoogleUser(String email, String name, String providerId) {
        UserInfo userInfo = UserInfo.builder()
                .nickname(name != null ? name : email.split("@")[0])
                .randomId(authService.generateUniqueRandomId())
                .build();
        userInfoRepository.save(userInfo);

        UserLoginInfo loginInfo = UserLoginInfo.builder()
                .userInfo(userInfo)
                .loginType(LoginType.GOOGLE)
                .email(email)
                .providerId(providerId)
                .build();
        userLoginInfoRepository.save(loginInfo);

        return loginInfo;
    }
}