//package com.duri.domain.auth.jwt.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.ArgumentMatchers.startsWith;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//import static org.mockito.Mockito.mockStatic;
//import static org.mockito.Mockito.spy;
//
//import com.duri.config.JwtConfig;
//import com.duri.domain.auth.CustomUserDetails;
//import com.duri.domain.auth.jwt.constant.TokenType;
//import com.duri.domain.user.entity.User;
//import com.duri.domain.user.service.UserService;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtBuilder;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import java.time.Duration;
//import java.util.Date;
//import javax.crypto.SecretKey;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//@ExtendWith(MockitoExtension.class)
//class JwtTokenServiceTest {
//
//    private final String REDIS_REFRESH_TOKEN_PREFIX = "RT:";
//    private final String testRefreshToken = "testRefreshToken";
//    private final Long testUserId = 1L;
//    private final String testUsername = "testUsername";
//    @InjectMocks
//    private JwtTokenService jwtTokenService;
//    @Mock
//    private RedisTemplate<String, String> redisTemplate;
//    @Mock
//    private JwtConfig jwtConfig;
//    @Mock
//    private UserService userService;
//    @Mock
//    private JwtUserDetailsService jwtUserDetailsService;
//    @Mock
//    private ValueOperations<String, String> valueOperations;
//    private CustomUserDetails userDetails;
//    private User user;
//
//    @BeforeEach
//    void setUp() {
//        user = User.builder()
//            .id(testUserId)
//            .username(testUsername)
//            .build();
//        userDetails = new CustomUserDetails(user);
//    }
//
//    @Test
//    void accessToken_생성_정상() {
//        // given
//        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//        given(jwtConfig.getSecretKey()).willReturn(secretKey);
//
//        // when
//        String token = jwtTokenService.createAccessToken(userDetails);
//
//        // then
//        assertThat(token).isNotNull();
//    }
//
//    @Test
//    void refreshToken_생성_정상() {
//        // given
//        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//        given(jwtConfig.getSecretKey()).willReturn(secretKey);
//        given(jwtConfig.getRefreshTokenExpiration()).willReturn(Duration.ofMinutes(10));
//        given(redisTemplate.opsForValue()).willReturn(valueOperations);
//
//        // 실제로 생성될 JWT
//        String fakeToken = testRefreshToken;
//        Date fakeExpiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60); // 1시간 뒤
//
//        try (MockedStatic<JwtUtil> mockedJwtUtil = mockStatic(JwtUtil.class)) {
//            // JwtUtil.getExpiration() 호출 시 고정값 리턴
//            mockedJwtUtil.when(() -> JwtUtil.getExpiration(eq(fakeToken), any()))
//                .thenReturn(fakeExpiration);
//
//            // JwtTokenService 내부 JWT 생성 로직 강제로 동일한 값 생성하도록 설정
//            // 실제 JWT 생성으로 테스트하면 SignatureKey가 매번 달라져 expiration 비교 어려움
//            try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class,
//                Mockito.CALLS_REAL_METHODS)) {
//                JwtBuilder builder = spy(Jwts.builder());
//                mockedJwts.when(Jwts::builder).thenReturn(builder);
//
//                // when
//                String result = jwtTokenService.createRefreshToken(userDetails, false);
//
//                // then
//                Assertions.assertThat(result).isNotNull();
//                then(valueOperations).should().set(startsWith(REDIS_REFRESH_TOKEN_PREFIX),
//                    eq(String.valueOf(testUserId)),
//                    any(Duration.class));
//
//            }
//        }
//    }
//
//    @Test
//    void refreshToken_유효성검사_성공() {
//        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//        given(jwtConfig.getSecretKey()).willReturn(secretKey);
//        given(redisTemplate.hasKey(any())).willReturn(true);
//        given(com.duri.domain.auth.jwt.util.JwtUtil.getTokenType(testRefreshToken,
//            secretKey)).willReturn(
//            TokenType.REFRESH);
//
//        boolean valid = jwtTokenService.isValidRefreshToken(testRefreshToken);
//
//        assertThat(valid).isTrue();
//    }
/// / /    @Test /    void refreshToken_유효성검사_실패() { /        // redis에 키 없음 /
/// given(redisTemplate.hasKey(any())).willReturn(false); / /        boolean valid =
/// jwtTokenService.isValidRefreshToken("invalid.token"); /        assertThat(valid).isFalse(); /
/// } / /    @Test /    void 리프레시_토큰_삭제_정상작동() { /        String token = "some.token"; /
/// jwtTokenService.deleteRefreshToken(token); /        verify(redisTemplate).delete("RT:" + token);
/// /    } / /    @Test /    void 로그아웃_정상작동() { /        HttpServletRequest request =
/// mock(HttpServletRequest.class); /        HttpServletResponse response =
/// mock(HttpServletResponse.class); / /        Cookie cookie = new Cookie("refreshToken",
/// "sample.refresh.token"); /        given(request.getCookies()).willReturn(new Cookie[]{cookie});
/// /        given(jwtConfig.getRefreshTokenCookieName()).willReturn("refreshToken"); /
/// given(redisTemplate.hasKey(any())).willReturn(true); / /        jwtTokenService.logout(request,
/// response); / /        verify(redisTemplate).delete(startsWith("RT:")); /    }
//
//    static class JwtUtil {
//
//        public static Date getExpiration(String token, SecretKey secretKey) {
//            return new Date();
//        }
//
//        public static Claims getPayload(String token, SecretKey key) {
//            return Jwts.claims().build();
//        }
//    }
//}
