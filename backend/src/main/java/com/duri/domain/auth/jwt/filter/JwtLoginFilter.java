package com.duri.domain.auth.jwt.filter;


import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.auth.jwt.dto.LoginRequest;
import com.duri.domain.auth.jwt.exception.AuthError;
import com.duri.domain.auth.jwt.service.JwtTokenService;
import com.duri.global.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

@RequiredArgsConstructor
@Slf4j
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest loginRequest = parseJsonLoginRequest(request);
            request.setAttribute("rememberMe", loginRequest.isRememberMe());

            String username = loginRequest.getEmailOrUsername();
            username = username != null ? username.trim() : "";

            String password = loginRequest.getPassword();
            password = password != null ? password : "";

            UsernamePasswordAuthenticationToken authToken = UsernamePasswordAuthenticationToken.unauthenticated(
                username, password);

            return authenticationManager.authenticate(authToken);
        } catch (JsonParseException e) {
            throw new AuthenticationException(e.getMessage(), e) {
            };
        }
    }

    private LoginRequest parseJsonLoginRequest(HttpServletRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return objectMapper.readValue(messageBody, LoginRequest.class);
        } catch (IOException e) {
            throw new JsonParseException();
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        boolean rememberMe = (boolean) request.getAttribute("rememberMe");

        String accessToken = jwtTokenService.createAccessToken(userDetails);
        String refreshToken = jwtTokenService.createRefreshToken(userDetails, rememberMe);

        jwtTokenService.setAccessToken(response, accessToken);
        jwtTokenService.setRefreshToken(response, refreshToken);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, AuthenticationException failed) throws IOException {

        ErrorResponse errorResponse;
        if (failed.getCause() instanceof JsonParseException jsonException) {
            errorResponse = ErrorResponse.of(jsonException);
        } else {
            errorResponse = ErrorResponse.of(AuthError.LOGIN_FAILED);
            errorResponse.addDetail("message", failed.getMessage());
        }
        response.setStatus(errorResponse.getStatus());
        response.setContentType("application/json;charset=UTF-8");
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .writeValue(response.getWriter(), errorResponse);
    }

}
