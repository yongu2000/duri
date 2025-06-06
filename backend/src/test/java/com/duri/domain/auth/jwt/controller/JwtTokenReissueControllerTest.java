package com.duri.domain.auth.jwt.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duri.config.JwtConfig;
import com.duri.domain.auth.exception.InvalidRefreshTokenException;
import com.duri.domain.auth.jwt.service.JwtTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@WebMvcTest(JwtTokenReissueController.class)
@DisplayName("토큰 재발급 Controller")
class JwtTokenReissueControllerTest {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
    private static final String VALID_REFRESH_TOKEN = "VALID_REFRESH_TOKEN";
    private static final String NEW_ACCESS_TOKEN = "NEW_ACCESS_TOKEN";
    private static final String NEW_REFRESH_TOKEN = "NEW_REFRESH_TOKEN";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JwtTokenService jwtTokenService;
    @MockitoBean
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        given(jwtConfig.getRefreshTokenCookieName()).willReturn(REFRESH_TOKEN_COOKIE_NAME);
        given(jwtConfig.getHeaderAuthorization()).willReturn(AUTHORIZATION_HEADER);
        given(jwtConfig.getAccessTokenPrefix()).willReturn(TOKEN_PREFIX);
    }

    @Test
    @DisplayName("/token/reissue - Access 토큰 재발급")
    void 유효한_리프레시_토큰_새로운_Access_Token_Refresh_Token_재발급() throws Exception {
        // given
        given(jwtTokenService.reissueAccessToken(VALID_REFRESH_TOKEN)).willReturn(
            NEW_ACCESS_TOKEN);
        given(jwtTokenService.reissueRefreshToken(VALID_REFRESH_TOKEN)).willReturn(
            NEW_REFRESH_TOKEN);

        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, VALID_REFRESH_TOKEN);

        doAnswer(invocation -> {
            HttpServletResponse res = invocation.getArgument(0);
            res.addHeader(AUTHORIZATION_HEADER, TOKEN_PREFIX + "newAccessToken");
            return null;
        }).when(jwtTokenService).setAccessToken(any(HttpServletResponse.class), anyString());

        doAnswer(invocation -> {
            HttpServletResponse res = invocation.getArgument(0);
            Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "newRefreshToken");
            res.addCookie(cookie);
            return null;
        }).when(jwtTokenService).setRefreshToken(any(), anyString());

        // when & then
        mockMvc.perform(post("/token/reissue")
                .cookie(refreshTokenCookie)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andDo(document("token-reissue",
                requestCookies(
                    cookieWithName(REFRESH_TOKEN_COOKIE_NAME).description("기존 리프레시 토큰")
                ),
                responseHeaders(
                    headerWithName(AUTHORIZATION_HEADER).description("재발급된 Access Token (Bearer)")
                ),
                responseCookies(
                    cookieWithName(REFRESH_TOKEN_COOKIE_NAME).description("재발급된 Refresh Token")
                )
            ));

        then(jwtTokenService).should().setAccessToken(any(HttpServletResponse.class),
            eq(NEW_ACCESS_TOKEN));
        then(jwtTokenService).should().setRefreshToken(any(HttpServletResponse.class),
            eq(NEW_REFRESH_TOKEN));
    }

    @Test
    @DisplayName("/token/reissue - Refresh 토큰이 없음 400 에러 반환")
    void 리프레시_토큰_없음_BAD_REQUEST() throws Exception {
        // given
        given(jwtTokenService.reissueAccessToken(null))
            .willThrow(new InvalidRefreshTokenException());

        // when & then
        mockMvc.perform(post("/token/reissue")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/token/reissue - 유효하지 않은 Refresh 토큰 401 에러 반환")
    void 유효하지_않은_리프레시_토큰_UNAUTHORIZED() throws Exception {
        // given
        String invalidToken = "invalid.refresh.token";
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, invalidToken);

        given(jwtTokenService.reissueAccessToken(invalidToken))
            .willThrow(new InvalidRefreshTokenException());

        // when & then
        mockMvc.perform(post("/token/reissue")
                .cookie(refreshTokenCookie)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    static class TestSecurityConfig { // 테스트 전용 Security 환경

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
}