// TokenResponse.java
package com.gachiganjik.gachiganjik_server.domain.user.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}