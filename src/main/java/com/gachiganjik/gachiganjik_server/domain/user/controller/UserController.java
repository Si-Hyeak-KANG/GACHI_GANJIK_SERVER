package com.gachiganjik.gachiganjik_server.domain.user.controller;

import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.domain.user.dto.*;
import com.gachiganjik.gachiganjik_server.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(userId)));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UpdateProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(userId, request)));
    }

    @PostMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<ProfileImageResponse>> updateProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileImageRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfileImage(userId, request.profileImageUrl())));
    }
}