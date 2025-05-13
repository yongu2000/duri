package com.duri.config;

import com.duri.domain.auth.exception.CustomAccessDeniedHandler;
import com.duri.domain.auth.exception.CustomAuthenticationEntryPoint;
import com.duri.domain.auth.jwt.filter.JwtAuthenticationFilter;
import com.duri.domain.auth.jwt.filter.JwtLoginFilter;
import com.duri.domain.auth.jwt.service.JwtTokenService;
import com.duri.domain.auth.oauth2.CustomOAuth2UserService;
import com.duri.domain.auth.oauth2.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private static final String[] PERMIT_URL_ARRAY = {
        "/error", "/login", "/join", "/token/reissue", "/email/**", "/user/password/reset",
        "/ws/**", "/uploads/**", "/sse/**"
    };
    private static final String[] PERMIT_GET_URL_ARRAY = {
        "/user/**"
    };
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenService jwtTokenService;
    private final JwtConfig jwtConfig;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${app.frontend.url}")
    private String FRONTEND_URL;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers(PERMIT_URL_ARRAY).permitAll()
                .requestMatchers(HttpMethod.GET, PERMIT_GET_URL_ARRAY).permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated())

            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenService, jwtConfig),
                JwtLoginFilter.class)
            .addFilterAt(
                new JwtLoginFilter(authenticationManager(authenticationConfiguration),
                    jwtTokenService),
                UsernamePasswordAuthenticationFilter.class)

            .oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                    .userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
                .failureHandler((request, response, exception) -> {
                    log.error("OAUTH2 LOGIN FAILURE");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                })
            )
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(authenticationEntryPoint)  // 인증 실패
                .accessDeniedHandler(accessDeniedHandler)           // 인가 실패
            )

            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .cors(
                corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();
                        // 프론트 서버 주소
                        configuration.setAllowedOrigins(
                            Collections.singletonList(FRONTEND_URL));
                        // GET, POST, 등 요청
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        // 쿠키, Authorization 인증 헤더, TLS client certificates(증명서)를 내포하는 자격 인증 정보
                        configuration.setAllowCredentials(true);
                        // 받을 수 있는 헤더 값
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        // 백엔드에서 프론트로 보낼 데이터들
                        configuration.setExposedHeaders(
                            Arrays.asList("Authorization", "Set-Cookie", "x-reissue-token"));

                        return configuration;
                    }
                }))
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
        throws Exception {
        return configuration.getAuthenticationManager();
    }
}
