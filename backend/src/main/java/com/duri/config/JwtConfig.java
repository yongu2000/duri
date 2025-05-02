package com.duri.config;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String issuer;
    private String secretKey;
    private String headerAuthorization;
    private String accessTokenPrefix;
    private String refreshTokenCookieName;

    private String accessTokenExpiration;
    private String refreshTokenExpiration;
    private String rememberMeRefreshTokenExpiration;

    private String rememberMeCookieName;

    private SecretKey cachedSecretKey;

    @PostConstruct
    public void init() {
        this.cachedSecretKey = new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8),
            Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    public Duration getAccessTokenExpiration() {
        return Duration.parse("PT" + accessTokenExpiration.toUpperCase());
    }

    public Duration getRefreshTokenExpiration() {
        return Duration.parse("P" + refreshTokenExpiration.toUpperCase());
    }

    public Duration getRememberMeRefreshTokenExpiration() {
        return Duration.parse("P" + rememberMeRefreshTokenExpiration.toUpperCase());
    }

    public String getAccessTokenPrefix() {
        return accessTokenPrefix + " ";
    }

    public SecretKey getSecretKey() {
        return cachedSecretKey;
    }

}
