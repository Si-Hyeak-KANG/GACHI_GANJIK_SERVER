package com.gachiganjik.gachiganjik_server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
        @NotBlank String idToken
) {}