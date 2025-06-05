package com.duri.domain.auth.jwt.filter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.duri.config.JwtConfig;
import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.auth.jwt.dto.LoginRequest;
import com.duri.domain.auth.jwt.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@DisplayName("로그인 필터 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JwtLoginFilterTest {

    private static final String NEW_ACCESS_TOKEN = "NEW_ACCESS_TOKEN";
    private static final String NEW_REFRESH_TOKEN = "NEW_REFRESH_TOKEN";
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private JwtConfig jwtConfig;
    private JwtLoginFilter jwtLoginFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtLoginFilter = new JwtLoginFilter(authenticationManager, jwtTokenService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void 로그인_성공_토큰_정상_발급() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("testUser", "password", false);
        String requestBody = new ObjectMapper().writeValueAsString(loginRequest);
        request.setContent(requestBody.getBytes());

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, Collections.emptyList());

        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtTokenService.createAccessToken(userDetails)).willReturn(NEW_ACCESS_TOKEN);
        given(jwtTokenService.createRefreshToken(userDetails, false)).willReturn(
            NEW_REFRESH_TOKEN);

        // when
        Authentication result = jwtLoginFilter.attemptAuthentication(request, response);
        jwtLoginFilter.successfulAuthentication(request, response, null, result);

        // then
        then(jwtTokenService).should().setAccessToken(response, NEW_ACCESS_TOKEN);
        then(jwtTokenService).should().setRefreshToken(response, NEW_REFRESH_TOKEN);
    }

    @Test
    void 로그인_실패_잘못된_인증_정보() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("wrongUser", "wrongPassword", false);
        String requestBody = new ObjectMapper().writeValueAsString(loginRequest);
        request.setContent(requestBody.getBytes());

        // when
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // then
        assertThrows(BadCredentialsException.class,
            () -> jwtLoginFilter.attemptAuthentication(request, response));
    }

}