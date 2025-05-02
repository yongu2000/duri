package com.duri.domain.auth.jwt.util;


import com.duri.domain.auth.jwt.constant.Claim;
import com.duri.domain.auth.jwt.constant.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import javax.crypto.SecretKey;

public class JwtUtil {

    public static TokenType getTokenType(String token, SecretKey secretKey) {
        return TokenType.valueOf(
            getPayload(token, secretKey).get(Claim.TOKEN_TYPE.getValue(), String.class));
    }

    public static Long getId(String token, SecretKey secretKey) {
        return getPayload(token, secretKey).get(Claim.ID.getValue(), Long.class);
    }

    public static Date getExpiration(String token, SecretKey secretKey) {
        return getPayload(token, secretKey).getExpiration();
    }

    private static Claims getPayload(String token, SecretKey secretKey) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

}
