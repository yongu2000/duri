package com.duri.domain.auth.jwt.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.duri.config.JwtConfig;
import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.auth.jwt.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 필터 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN_REISSUE_URL = "/api/token/reissue";
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private JwtConfig jwtConfig;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenService, jwtConfig);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        // 테스트 후 SecurityContext 정리
        SecurityContextHolder.clearContext();
    }

    @Test
    void 토큰_재발급_요청_필터_건너뛰기() throws Exception {
        // given
        request.setRequestURI(TOKEN_REISSUE_URL);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenService); // 토큰 검증 로직이 실행되지 않아야 함
    }

    @Test
    void 유효한_Access_Token_인증_성공() throws Exception {
        // given
        String token = "valid-access-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtConfig.getHeaderAuthorization()).thenReturn("Authorization");
        when(jwtConfig.getAccessTokenPrefix()).thenReturn("Bearer ");

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(jwtTokenService.isValidAccessToken(token)).thenReturn(true);
        when(jwtTokenService.getUserDetailsFromToken(token)).thenReturn(userDetails);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void 만료된_Access_Token_유효한_Refresh_Token_토큰_재발급_요청() throws Exception {
        // given
        String accessToken = "expired-access-token";
        String refreshToken = "valid-refresh-token";

        when(jwtConfig.getHeaderAuthorization()).thenReturn("Authorization");
        when(jwtConfig.getAccessTokenPrefix()).thenReturn("Bearer ");
        when(jwtConfig.getRefreshTokenCookieName()).thenReturn("refresh-token");

        request.addHeader("Authorization", "Bearer " + accessToken);
        request.setCookies(new Cookie("refresh-token", refreshToken));

        when(jwtTokenService.isValidAccessToken(accessToken)).thenReturn(false);
        when(jwtTokenService.isValidRefreshToken(refreshToken)).thenReturn(true);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getHeader("x-reissue-token")).isEqualTo("true");
        verify(filterChain, never()).doFilter(request, response); // 필터 체인이 진행되지 않아야 함
    }

    @Test
    void 두_토큰_모두_만료시_인증_발급하지_않음() throws Exception {
        // given
        String accessToken = "invalid-access-token";
        String refreshToken = "invalid-refresh-token";

        when(jwtConfig.getHeaderAuthorization()).thenReturn("Authorization");
        when(jwtConfig.getAccessTokenPrefix()).thenReturn("Bearer ");
        when(jwtConfig.getRefreshTokenCookieName()).thenReturn("refresh-token");

        request.addHeader("Authorization", "Bearer " + accessToken);
        request.setCookies(new Cookie("refresh-token", refreshToken));

        when(jwtTokenService.isValidAccessToken(accessToken)).thenReturn(false);
        when(jwtTokenService.isValidRefreshToken(refreshToken)).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void 토큰_없는_요청_인증_발급하지_않음() throws Exception {
        // given
        request.setCookies((Cookie[]) null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}