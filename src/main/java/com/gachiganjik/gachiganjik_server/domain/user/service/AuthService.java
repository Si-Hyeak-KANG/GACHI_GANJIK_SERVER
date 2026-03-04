package com.gachiganjik.gachiganjik_server.domain.user.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.common.security.JwtProvider;
import com.gachiganjik.gachiganjik_server.domain.user.dto.*;
import com.gachiganjik.gachiganjik_server.domain.user.entity.*;
import com.gachiganjik.gachiganjik_server.domain.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final UserInfoRepository userInfoRepository;
    private final UserLoginInfoRepository userLoginInfoRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userLoginInfoRepository.existsByEmailAndLoginType(request.email(), LoginType.EMAIL)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserInfo userInfo = UserInfo.builder()
                .nickname(request.nickname())
                .randomId(generateUniqueRandomId())
                .build();
        userInfoRepository.save(userInfo);

        UserLoginInfo loginInfo = UserLoginInfo.builder()
                .userInfo(userInfo)
                .loginType(LoginType.EMAIL)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        userLoginInfoRepository.save(loginInfo);

        return issueTokens(userInfo);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserLoginInfo loginInfo = userLoginInfoRepository
                .findByEmailAndLoginType(request.email(), LoginType.EMAIL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), loginInfo.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(loginInfo.getUserInfo());
    }

    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        UserSession session = userSessionRepository
                .findBySessionToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (session.getStatus() != UserSessionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (session.getExpireDt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        session.logout();

        UserInfo userInfo = session.getUserInfo();
        String newAccessToken = jwtProvider.generateAccessToken(userInfo.getUserId());
        String newRefreshToken = jwtProvider.generateRefreshToken(userInfo.getUserId());

        saveSession(userInfo, newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        userSessionRepository.logoutAllByUserIdAndStatus(userId, UserSessionStatus.ACTIVE);
    }

    private AuthResponse issueTokens(UserInfo userInfo) {
        userSessionRepository.logoutAllByUserIdAndStatus(userInfo.getUserId(), UserSessionStatus.ACTIVE);

        String accessToken = jwtProvider.generateAccessToken(userInfo.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(userInfo.getUserId());

        saveSession(userInfo, refreshToken);

        return new AuthResponse(userInfo.getUserId(), userInfo.getNickname(), accessToken, refreshToken);
    }

    public String[] issueTokensForSocial(UserInfo userInfo) {
        userSessionRepository.logoutAllByUserIdAndStatus(userInfo.getUserId(), UserSessionStatus.ACTIVE);

        String accessToken = jwtProvider.generateAccessToken(userInfo.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(userInfo.getUserId());

        saveSession(userInfo, refreshToken);

        return new String[]{accessToken, refreshToken};
    }

    private void saveSession(UserInfo userInfo, String refreshToken) {
        UserSession session = UserSession.builder()
                .userInfo(userInfo)
                .sessionToken(refreshToken)
                .expireDt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000))
                .build();
        userSessionRepository.save(session);
    }

    public String generateUniqueRandomId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        String randomId;
        do {
            randomId = "#" + random.ints(7, 0, chars.length())
                    .mapToObj(i -> String.valueOf(chars.charAt(i)))
                    .reduce("", String::concat);
        } while (userInfoRepository.findByRandomId(randomId).isPresent());
        return randomId;
    }

    public AuthResponse issueTokensAsAuthResponse(UserInfo userInfo) {
        return issueTokens(userInfo);
    }

}