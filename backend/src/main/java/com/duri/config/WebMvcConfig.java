package com.duri.config;

import com.duri.global.converter.DecryptIdConverter;
import com.duri.global.log.LoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    @Value("${app.frontend.url}")
    private String FRONTEND_URL;

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry
            // CORS를 적용할 URL 패턴
            .addMapping("/**")
            // 응답에 노출되는 헤더
            .exposedHeaders("Authorization", "Set-Cookie", "x-reissue-token")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            // 자원 공유를 허락할 origin (프론트)
            .allowCredentials(true)
            .allowedOrigins(FRONTEND_URL);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**") // 클라이언트가 접근할 경로
            .addResourceLocations("file:uploads/"); // 실제 로컬 디렉토리
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new DecryptIdConverter());
    }
}
