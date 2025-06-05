package com.duri.domain.auth.jwt.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duri.domain.auth.jwt.service.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@WebMvcTest(JwtLogoutController.class)
@DisplayName("JWT 로그아웃 Controller")
@AutoConfigureMockMvc(addFilters = false)
class JwtLogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Test
    @DisplayName("/logout - 로그아웃 (refresh token 쿠키)")
    void 로그아웃_성공() throws Exception {
        // when
        mockMvc.perform(post("/logout")
                .header("Cookie", "refreshToken=sample-refresh-token"))
            .andExpect(status().isOk())
            .andDo(document("jwt-logout",
                requestHeaders(
                    headerWithName("Cookie").description("리프레시 토큰이 담긴 쿠키 (refreshToken=...)")
                )
            ));

        // then
        then(jwtTokenService).should()
            .logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
}