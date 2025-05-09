package com.duri.domain.auth.jwt.filter;


import com.duri.config.JwtConfig;
import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.auth.jwt.service.JwtTokenService;
import com.duri.global.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_REISSUE_URL = "/token/reissue";
    private static final String WEBSOCKET_URL = "/ws";
    private final JwtTokenService jwtTokenService;
    private final JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // /api/token/reissue 요청은 필터를 건너뛰도록 예외 처리
        if (requestURI.startsWith(WEBSOCKET_URL) || requestURI.equals(TOKEN_REISSUE_URL)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader(jwtConfig.getHeaderAuthorization());
        String accessToken = getAccessToken(authorizationHeader);

        boolean isAccessTokenValid =
            accessToken != null && jwtTokenService.isValidAccessToken(accessToken);

        if (isAccessTokenValid) {
            CustomUserDetails userDetails = jwtTokenService.getUserDetailsFromToken(accessToken);
            Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = CookieUtil.getCookieByName(request.getCookies(),
            jwtConfig.getRefreshTokenCookieName());
        boolean isRefreshTokenValid =
            refreshToken != null && jwtTokenService.isValidRefreshToken(refreshToken);

        // AccessToken이 만료되었지만 RefreshToken이 유효한 경우 프론트에 재발급 요청 신호 보내기
        if (isRefreshTokenValid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("x-reissue-token", "true");  // 프론트에서 감지해서 자동으로 재발급 요청
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(
            jwtConfig.getAccessTokenPrefix())) {
            return authorizationHeader.substring(jwtConfig.getAccessTokenPrefix().length());
        }
        return null;
    }
}
