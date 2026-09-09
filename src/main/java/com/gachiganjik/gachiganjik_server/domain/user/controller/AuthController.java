package com.gachiganjik.gachiganjik_server.domain.user.controller;

import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.domain.user.dto.*;
import com.gachiganjik.gachiganjik_server.domain.user.service.AuthService;
import com.gachiganjik.gachiganjik_server.domain.user.service.SocialAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SocialAuthService socialAuthService;

    /**
     * 이메일 회원가입. 이메일 인증 완료가 선행되어야 한다.
     * 검증된 이메일로 기존 계정이 있으면 계정을 만들지 않고 연결 동의를 요청한다.
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 기존 계정에 EMAIL 로그인 수단을 연결한다.
     * 비밀번호는 서버가 보관하지 않으므로 클라이언트가 다시 전달한다.
     */
    @PostMapping("/link/email")
    public ResponseEntity<ApiResponse<AuthResponse>> linkEmailAccount(
            @Valid @RequestBody EmailLinkRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.linkEmailAccount(request)));
    }

    /**
     * 소셜 로그인. provider 는 google | kakao | naver.
     */
    @PostMapping("/social/{provider}")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> socialLogin(
            @PathVariable String provider,
            @RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(socialAuthService.login(provider, request)));
    }

    /**
     * 소셜 신규 가입 완료. 닉네임만 받아 계정을 생성한다.
     */
    @PostMapping("/social/complete")
    public ResponseEntity<ApiResponse<AuthResponse>> completeSocialSignup(
            @Valid @RequestBody SocialSignupCompleteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(socialAuthService.completeSignup(request)));
    }

    /**
     * 기존 계정에 소셜 로그인 수단을 연결한다.
     */
    @PostMapping("/social/link")
    public ResponseEntity<ApiResponse<AuthResponse>> linkSocialAccount(
            @Valid @RequestBody SocialLinkRequest request) {
        return ResponseEntity.ok(ApiResponse.success(socialAuthService.linkAccount(request)));
    }
}