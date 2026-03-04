// TokenRefreshRequest.java
package com.gachiganjik.gachiganjik_server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank String refreshToken
) {}