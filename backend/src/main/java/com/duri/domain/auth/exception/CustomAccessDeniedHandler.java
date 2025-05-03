package com.duri.domain.auth.exception;

import com.duri.domain.auth.jwt.exception.AuthError;
import com.duri.global.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException {
        log.error("CustomAccessDeniedHandler called");
        ErrorResponse errorResponse = ErrorResponse.of(AuthError.ACCESS_DENIED);

        // 추가 정보 기록
        errorResponse.addDetail("requiredRole", "ROLE_REQUIRED");  // 필요한 권한 정보
        errorResponse.addDetail("path", request.getRequestURI());  // 요청 경로

        response.setStatus(errorResponse.getStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
