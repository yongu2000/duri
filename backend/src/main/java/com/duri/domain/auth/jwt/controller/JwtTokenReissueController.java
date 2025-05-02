package com.duri.domain.auth.jwt.controller;

import com.duri.config.JwtConfig;
import com.duri.domain.auth.jwt.service.JwtTokenService;
import com.duri.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Slf4j
public class JwtTokenReissueController {

    private final JwtTokenService jwtTokenService;
    private final JwtConfig jwtConfig;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueAccessTokenAtHeader(HttpServletRequest request,
        HttpServletResponse response) {

        String refreshToken = CookieUtil.getCookieByName(request.getCookies(),
            jwtConfig.getRefreshTokenCookieName());
        String newAccessToken = jwtTokenService.reissueAccessToken(refreshToken);

        String newRefreshToken = jwtTokenService.reissueRefreshToken(refreshToken);

        jwtTokenService.setAccessToken(response, newAccessToken);
        jwtTokenService.setRefreshToken(response, newRefreshToken);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
