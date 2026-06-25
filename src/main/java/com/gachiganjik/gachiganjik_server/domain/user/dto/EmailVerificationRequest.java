package com.gachiganjik.gachiganjik_server.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmailVerificationRequest {

    public record SendRequest(
            @NotBlank @Email String email
    ) {}

    public record VerifyRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 6) String code
    ) {}
}