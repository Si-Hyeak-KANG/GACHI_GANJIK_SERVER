package com.gachiganjik.gachiganjik_server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SocialSignupCompleteRequest(
        @NotBlank String signupTicket,
        @NotBlank @Size(min = 2, max = 20) String nickname
) {}