package com.duri.domain.auth.exception;

import com.duri.global.dto.ErrorResponse;
import com.duri.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {

        log.error("CustomAuthenticationEntryPoint called");

        ErrorCode errorCode = AuthError.UNAUTHORIZED;

        // OAuth2 인증 실패인 경우 별도 처리
        if (authException instanceof OAuth2AuthenticationException) {
            log.error("OAuth2 error");

            OAuth2Error oauth2Error = ((OAuth2AuthenticationException) authException).getError();
            ErrorResponse errorResponse = ErrorResponse.of(errorCode);
            errorResponse.addDetail("oauth2_error", oauth2Error.getErrorCode());
            errorResponse.addDetail("oauth2_description", oauth2Error.getDescription());
            sendErrorResponse(response, errorResponse);
            return;
        }

        // JWT 토큰 관련 예외 처리
        if (authException.getCause() instanceof JwtException) {
            log.error("JWT Token error");

            errorCode = AuthError.INVALID_TOKEN;
        }

        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        log.error("{}", errorResponse.getMessage());

        sendErrorResponse(response, errorResponse);
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorResponse errorResponse)
        throws IOException {
        response.setStatus(errorResponse.getStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
