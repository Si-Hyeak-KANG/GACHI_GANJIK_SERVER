package com.gachiganjik.gachiganjik_server.common.security;

import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.common.response.ApiResponse;
import com.gachiganjik.gachiganjik_server.common.response.ErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 인증되지 않은 요청(토큰 없음/유효하지 않음)이 보호된 엔드포인트에 접근했을 때
 * 다른 API 에러와 동일한 {@link ApiResponse} 포맷으로 401 응답을 내려준다.
 *
 * @see com.gachiganjik.gachiganjik_server.common.exception.GlobalExceptionHandler
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                          HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        log.warn("Unauthenticated request: {} {}", request.getMethod(), request.getRequestURI());

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.fail(ErrorDetail.of(errorCode)))
        );
    }
}
