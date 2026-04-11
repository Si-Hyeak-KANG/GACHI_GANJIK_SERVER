package com.gachiganjik.gachiganjik_server.domain.user.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.user.dto.*;
import com.gachiganjik.gachiganjik_server.domain.user.entity.LoginType;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserInfo;
import com.gachiganjik.gachiganjik_server.domain.user.entity.UserLoginInfo;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserInfoRepository;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserLoginInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserInfoRepository userInfoRepository;
    private final UserLoginInfoRepository userLoginInfoRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getMyProfile(Long userId) {
        UserInfo userInfo = findUserById(userId);
        // 이메일 로그인 우선, 없으면 소셜(Google 등) 첫 번째 loginInfo 사용
        UserLoginInfo loginInfo = userLoginInfoRepository
                .findByUserInfoAndLoginType(userInfo, LoginType.EMAIL)
                .orElseGet(() -> userLoginInfoRepository
                        .findFirstByUserInfo(userInfo)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)));

        return UserProfileResponse.of(userInfo, loginInfo);
    }

    @Transactional
    public UpdateProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserInfo userInfo = findUserById(userId);

        String newNickname = StringUtils.hasText(request.nickname()) ? request.nickname() : null;

        if (StringUtils.hasText(request.newPassword())) {
            validatePasswordChange(userInfo, request);
            updatePassword(userInfo, request.newPassword());
        }

        userInfo.updateProfile(newNickname, null);
        return UpdateProfileResponse.of(userInfo);
    }

    @Transactional
    public ProfileImageResponse updateProfileImage(Long userId, String profileImageUrl) {
        UserInfo userInfo = findUserById(userId);
        userInfo.updateProfile(null, profileImageUrl);
        return new ProfileImageResponse(userInfo.getProfileImageUrl());
    }

    private void validatePasswordChange(UserInfo userInfo, UpdateProfileRequest request) {
        UserLoginInfo loginInfo = userLoginInfoRepository
                .findByUserInfoAndLoginType(userInfo, LoginType.EMAIL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.currentPassword(), loginInfo.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!request.newPassword().equals(request.passwordConfirm())) {
            throw new BusinessException(ErrorCode.WEAK_PASSWORD);
        }
    }

    private void updatePassword(UserInfo userInfo, String newPassword) {
        UserLoginInfo loginInfo = userLoginInfoRepository
                .findByUserInfoAndLoginType(userInfo, LoginType.EMAIL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        loginInfo.updatePassword(passwordEncoder.encode(newPassword));
    }

    private UserInfo findUserById(Long userId) {
        return userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}