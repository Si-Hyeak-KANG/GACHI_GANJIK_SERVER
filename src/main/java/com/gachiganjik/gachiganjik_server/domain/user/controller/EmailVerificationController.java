package com.gachiganjik.gachiganjik_server.domain.user.controller;

import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.domain.user.dto.EmailVerificationRequest;
import com.gachiganjik.gachiganjik_server.domain.user.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<?>> sendCode(
            @Valid @RequestBody EmailVerificationRequest.SendRequest request) {
        emailVerificationService.sendCode(request.email());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyCode(
            @Valid @RequestBody EmailVerificationRequest.VerifyRequest request) {
        emailVerificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok(ApiResponse.success());
    }
}