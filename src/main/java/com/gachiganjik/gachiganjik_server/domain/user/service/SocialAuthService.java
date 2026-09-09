package com.gachiganjik.gachiganjik_server.domain.user.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.user.dto.AuthResponse;
import com.gachiganjik.gachiganjik_server.domain.user.dto.SocialLinkRequest;
import com.gachiganjik.gachiganjik_server.domain.user.dto.SocialLoginRequest;
import com.gachiganjik.gachiganjik_server.domain.user.dto.SocialLoginResponse;
import com.gachiganjik.gachiganjik_server.domain.user.dto.SocialSignupCompleteRequest;
import com.gachiganjik.gachiganjik_server.domain.user.dto.SocialUserProfile;
import com.gachiganjik.gachiganjik_server.domain.user.entity.LinkTicket;
import com.gachiganjik.gachiganjik_server.domain.user.entity.LoginType;
import com.gachiganjik.gachiganjik_server.domain.user.entity.SignupTicket;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserLoginInfo;
import com.gachiganjik.gachiganjik_server.domain.user.repository.LinkTicketRepository;
import com.gachiganjik.gachiganjik_server.domain.user.repository.SignupTicketRepository;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserInfoRepository;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserLoginInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

/**
 * 소셜 로그인 오케스트레이션.
 *
 * 1) /social/{provider} : 제공자 토큰 검증 후 3분기
 *      - (provider, providerUserId) 가 이미 있으면 로그인
 *      - 검증된 이메일로 기존 계정이 있으면 연결 동의 요청
 *      - 그 외에는 신규 가입 티켓 발급
 * 2) /social/complete   : 닉네임을 받아 계정 생성
 * 3) /social/link       : 사용자 동의 후 기존 계정에 소셜 수단 연결
 *
 * 계정 병합 키는 검증된 이메일뿐이다. 미검증/부재 이메일로는 절대 연결하지 않는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAuthService {

    private final UserInfoRepository userInfoRepository;
    private final UserLoginInfoRepository userLoginInfoRepository;
    private final SignupTicketRepository signupTicketRepository;
    private final LinkTicketRepository linkTicketRepository;
    private final GoogleAuthService googleAuthService;
    private final KakaoAuthService kakaoAuthService;
    private final NaverAuthService naverAuthService;
    private final AuthService authService;

    @Value("${social.signup-ticket.ttl-seconds}")
    private long signupTicketTtlSeconds;

    @Value("${social.link-ticket.ttl-seconds}")
    private long linkTicketTtlSeconds;

    @Transactional
    public SocialLoginResponse login(String provider, SocialLoginRequest request) {
        SocialUserProfile profile = fetchProfile(resolveProvider(provider), request);

        Optional<UserLoginInfo> linked = userLoginInfoRepository
                .findByProviderIdAndLoginType(profile.providerUserId(), profile.provider());
        if (linked.isPresent()) {
            return loginExistingUser(linked.get(), profile);
        }

        if (profile.emailVerified() && StringUtils.hasText(profile.email())) {
            Optional<UserInfo> linkTarget = userInfoRepository.findByEmail(profile.email());
            if (linkTarget.isPresent()) {
                return issueLinkTicket(profile, linkTarget.get());
            }
        }

        return issueSignupTicket(profile);
    }

    @Transactional
    public AuthResponse completeSignup(SocialSignupCompleteRequest request) {
        SignupTicket ticket = signupTicketRepository.findById(request.signupTicket())
                .orElseThrow(() -> new BusinessException(ErrorCode.SIGNUP_TICKET_NOT_FOUND));

        signupTicketRepository.delete(ticket);

        UserInfo userInfo = UserInfo.builder()
                .nickname(request.nickname())
                .profileImageUrl(ticket.getProfileImageUrl())
                .randomId(authService.generateUniqueRandomId())
                .email(ticket.isEmailVerified() ? ticket.getEmail() : null)
                .build();
        userInfoRepository.save(userInfo);

        saveSocialLoginInfo(userInfo, ticket.getProvider(), ticket.getProviderUserId(), ticket.getEmail());

        return authService.issueTokensAsAuthResponse(userInfo);
    }

    @Transactional
    public AuthResponse linkAccount(SocialLinkRequest request) {
        LinkTicket ticket = linkTicketRepository.findById(request.linkTicket())
                .orElseThrow(() -> new BusinessException(ErrorCode.LINK_TICKET_NOT_FOUND));

        if (ticket.getMethod() == LoginType.EMAIL) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        UserInfo userInfo = userInfoRepository.findById(ticket.getTargetUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userLoginInfoRepository.findByUserInfoAndLoginType(userInfo, ticket.getMethod()).isPresent()) {
            throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_CONFLICT);
        }

        saveSocialLoginInfo(userInfo, ticket.getMethod(), ticket.getProviderUserId(), ticket.getEmail());
        linkTicketRepository.delete(ticket);

        return authService.issueTokensAsAuthResponse(userInfo);
    }

    /**
     * 경로 변수를 LoginType 으로 변환한다.
     * EMAIL 은 소셜 제공자가 아니므로 /social/email 요청은 거부한다.
     */
    private LoginType resolveProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        LoginType loginType;
        try {
            loginType = LoginType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (loginType == LoginType.EMAIL) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return loginType;
    }

    private SocialUserProfile fetchProfile(LoginType provider, SocialLoginRequest request) {
        return switch (provider) {
            case GOOGLE -> googleAuthService.fetchProfile(requireToken(request.idToken()));
            case KAKAO  -> kakaoAuthService.fetchProfile(requireToken(request.accessToken()));
            case NAVER  -> naverAuthService.fetchProfile(requireToken(request.accessToken()));
            case EMAIL  -> throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        };
    }

    private String requireToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return token;
    }

    private SocialLoginResponse loginExistingUser(UserLoginInfo loginInfo, SocialUserProfile profile) {
        loginInfo.getUserInfo().updateProfile(null, profile.profileImageUrl());

        return SocialLoginResponse.existingUser(
                authService.issueTokensAsAuthResponse(loginInfo.getUserInfo()));
    }

    private SocialLoginResponse issueLinkTicket(SocialUserProfile profile, UserInfo targetUser) {
        String ticketId = UUID.randomUUID().toString();

        linkTicketRepository.save(new LinkTicket(
                ticketId,
                profile.provider(),
                profile.providerUserId(),
                profile.email(),
                targetUser.getUserId(),
                null,
                linkTicketTtlSeconds
        ));

        return SocialLoginResponse.needsLink(ticketId, EmailMasker.mask(profile.email()));
    }

    private SocialLoginResponse issueSignupTicket(SocialUserProfile profile) {
        String ticketId = UUID.randomUUID().toString();

        signupTicketRepository.save(new SignupTicket(
                ticketId,
                profile.provider(),
                profile.providerUserId(),
                profile.nickname(),
                profile.email(),
                profile.emailVerified(),
                profile.profileImageUrl(),
                signupTicketTtlSeconds
        ));

        return SocialLoginResponse.newUser(ticketId, profile);
    }

    private void saveSocialLoginInfo(UserInfo userInfo, LoginType provider,
                                     String providerUserId, String email) {
        userLoginInfoRepository.save(UserLoginInfo.builder()
                .userInfo(userInfo)
                .loginType(provider)
                .email(email)
                .providerId(providerUserId)
                .build());
    }
}