package com.gachiganjik.gachiganjik_server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 기존 계정에 EMAIL 로그인 수단을 붙인다.
 * 비밀번호는 서버가 보관하지 않으므로, 가입 화면에서 입력한 값을 클라이언트가 다시 실어 보낸다.
 */
public record EmailLinkRequest(
        @NotBlank String linkTicket,
        @NotBlank String password
) {}