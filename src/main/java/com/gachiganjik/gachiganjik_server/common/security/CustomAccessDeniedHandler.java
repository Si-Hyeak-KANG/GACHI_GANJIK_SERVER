package com.gachiganjik.gachiganjik_server.common.security;

import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.common.response.ErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 인증은 되었으나 권한이 없는 요청이 보호된 엔드포인트에 접근했을 때
 * 다른 API 에러와 동일한 {@link ApiResponse} 포맷으로 403 응답을 내려준다.
 *
 * 인증 주체가 {@link GuestPrincipal}인 경우, 계약상 게스트에게 허용되지 않은
 * (guestKeyAuth를 선언하지 않은) 엔드포인트에 접근한 것이므로 게스트 전용 코드
 * {@link ErrorCode#GUEST_NOT_ALLOWED}를 반환한다(ADR-020, ADR-021). 그 외(회원인데
 * 역할/권한 부족 등 다른 사유로 거부된 경우)는 기존 {@link ErrorCode#FORBIDDEN}을 반환한다.
 *
 * @see com.gachiganjik.gachiganjik_server.common.exception.GlobalExceptionHandler
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                        HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied: {} {}", request.getMethod(), request.getRequestURI());

        ErrorCode errorCode = isGuest() ? ErrorCode.GUEST_NOT_ALLOWED : ErrorCode.FORBIDDEN;
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.fail(ErrorDetail.of(errorCode)))
        );
    }

    private boolean isGuest() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        return principal instanceof GuestPrincipal;
    }
}
