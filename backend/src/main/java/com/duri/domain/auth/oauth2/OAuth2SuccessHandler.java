package com.duri.domain.auth.oauth2;


import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.auth.jwt.service.JwtTokenService;
import com.duri.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    @Value("${app.frontend.url}")
    private String FRONTEND_URL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        userService.findByUsername(userDetails.getUsername());

        String refreshToken = jwtTokenService.createRefreshToken(userDetails, false);
        jwtTokenService.setRefreshToken(response, refreshToken);
        response.sendRedirect(FRONTEND_URL);
        log.info("OAuth2.0 Login Success = {}", refreshToken);
        log.info("OAuth2.0 Login Success = {}, {}", userDetails.getUsername(),
            userDetails.getEmail());
    }
}
