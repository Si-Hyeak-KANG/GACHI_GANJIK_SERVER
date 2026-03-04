// AuthResponse.java
package com.gachiganjik.gachiganjik_server.domain.user.dto;

public record AuthResponse(
        Long userId,
        String nickname,
        String accessToken,
        String refreshToken
) {}