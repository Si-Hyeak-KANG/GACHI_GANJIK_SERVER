package com.gachiganjik.gachiganjik_server.domain.user.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.common.security.JwtProvider;
import com.gachiganjik.gachiganjik_server.domain.guest.service.GuestService;
import com.gachiganjik.gachiganjik_server.domain.user.dto.*;
import com.gachiganjik.gachiganjik_server.domain.user.entity.*;
import com.gachiganjik.gachiganjik_server.domain.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${social.link-ticket.ttl-seconds}")
    private long linkTicketTtlSeconds;

    private final UserInfoRepository userInfoRepository;
    private final UserLoginInfoRepository userLoginInfoRepository;
    private final UserSessionRepository userSessionRepository;
    private final VerifiedEmailRepository verifiedEmailRepository;
    private final LinkTicketRepository linkTicketRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final GuestService guestService;

    /**
     * 이메일 회원가입.
     * 인증 마커가 없으면 거부한다. 마커 없이 통과시키면 /auth/email/verify 를 건너뛴
     * 직접 호출로 임의의 이메일 계정을 만들 수 있고, 이메일이 계정 병합 키라 탈취로 이어진다.
     */
    @Transactional
    public SignupResponse register(RegisterRequest request) {
        assertEmailVerified(request.email());

        if (userLoginInfoRepository.existsByEmailAndLoginType(request.email(), LoginType.EMAIL)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Optional<UserInfo> linkTarget = userInfoRepository.findByEmail(request.email());
        if (linkTarget.isPresent()) {
            String linkTicket = issueEmailLinkTicket(request, linkTarget.get());
            consumeVerifiedEmail(request.email());
            return SignupResponse.needsLink(linkTicket, EmailMasker.mask(request.email()));
        }

        UserInfo userInfo = UserInfo.builder()
                .nickname(request.nickname())
                .randomId(generateUniqueRandomId())
                .email(request.email())
                .build();
        userInfoRepository.save(userInfo);

        userLoginInfoRepository.save(UserLoginInfo.builder()
                .userInfo(userInfo)
                .loginType(LoginType.EMAIL)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build());

        if (StringUtils.hasText(request.guestKey())) {
            guestService.convertToMember(request.guestKey(), userInfo);
        }

        consumeVerifiedEmail(request.email());
        return SignupResponse.created(issueTokens(userInfo));
    }

    /**
     * 기존 계정에 EMAIL 로그인 수단을 붙인다.
     * 비밀번호는 티켓에 담지 않으므로 클라이언트가 다시 전달한 값을 사용한다.
     */
    @Transactional
    public AuthResponse linkEmailAccount(EmailLinkRequest request) {
        LinkTicket ticket = linkTicketRepository.findById(request.linkTicket())
                .orElseThrow(() -> new BusinessException(ErrorCode.LINK_TICKET_NOT_FOUND));

        if (ticket.getMethod() != LoginType.EMAIL) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        UserInfo userInfo = userInfoRepository.findById(ticket.getTargetUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userLoginInfoRepository.findByUserInfoAndLoginType(userInfo, LoginType.EMAIL).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        userLoginInfoRepository.save(UserLoginInfo.builder()
                .userInfo(userInfo)
                .loginType(LoginType.EMAIL)
                .email(ticket.getEmail())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build());

        if (StringUtils.hasText(ticket.getGuestKey())) {
            guestService.convertToMember(ticket.getGuestKey(), userInfo);
        }

        linkTicketRepository.delete(ticket);
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

    public AuthResponse issueTokensAsAuthResponse(UserInfo userInfo) {
        return issueTokens(userInfo);
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

    private void assertEmailVerified(String email) {
        if (verifiedEmailRepository.findById(email).isEmpty()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    private void consumeVerifiedEmail(String email) {
        verifiedEmailRepository.deleteById(email);
    }

    private String issueEmailLinkTicket(RegisterRequest request, UserInfo targetUser) {
        String ticketId = UUID.randomUUID().toString();

        linkTicketRepository.save(new LinkTicket(
                ticketId,
                LoginType.EMAIL,
                null,
                request.email(),
                targetUser.getUserId(),
                request.guestKey(),
                linkTicketTtlSeconds
        ));

        return ticketId;
    }

    private AuthResponse issueTokens(UserInfo userInfo) {
        userSessionRepository.logoutAllByUserIdAndStatus(userInfo.getUserId(), UserSessionStatus.ACTIVE);

        String accessToken = jwtProvider.generateAccessToken(userInfo.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(userInfo.getUserId());

        saveSession(userInfo, refreshToken);

        return new AuthResponse(userInfo.getUserId(), userInfo.getNickname(), accessToken, refreshToken);
    }

    private void saveSession(UserInfo userInfo, String refreshToken) {
        UserSession session = UserSession.builder()
                .userInfo(userInfo)
                .sessionToken(refreshToken)
                .expireDt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000))
                .build();
        userSessionRepository.save(session);
    }
}