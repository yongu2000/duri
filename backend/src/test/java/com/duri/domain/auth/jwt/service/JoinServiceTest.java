package com.duri.domain.auth.jwt.service;

import static com.duri.domain.email.constant.EmailRedisKey.EMAIL_VERIFIED_KEY;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.duri.domain.auth.exception.DuplicateUserException;
import com.duri.domain.auth.exception.EmailNotVerifiedException;
import com.duri.domain.auth.jwt.dto.JoinRequest;
import com.duri.domain.user.entity.Gender;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원가입 서비스 단위 테스트")
@DisplayNameGeneration(ReplaceUnderscores.class)
class JoinServiceTest {

    private final String testEmail = "test@example.com";
    private final String testUsername = "test";
    private final String testName = "Test User";
    private final String testPassword = "Test Password";
    private final Gender testGender = Gender.MALE;
    private final LocalDate testBirthday = LocalDate.of(2000, 4, 18);
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private JoinService joinService;

    @Test
    void 회원가입_성공() {
        // given
        JoinRequest request = new JoinRequest(testEmail, testUsername, testPassword, testName,
            testGender, testBirthday);
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
        given(userRepository.findByUsername(request.getUsername())).willReturn(Optional.empty());
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EMAIL_VERIFIED_KEY + request.getEmail())).willReturn("true");
        given(bCryptPasswordEncoder.encode(request.getPassword())).willReturn("encodedPw");

        // when
        joinService.join(request);

        // then
        then(userRepository).should().save(any(User.class));
    }

    @Test
    void 이메일_중복시_예외_발생() {
        // given
        JoinRequest request = new JoinRequest(testEmail, testUsername, testPassword, testName,
            testGender, testBirthday);
        given(userRepository.findByEmail(request.getEmail())).willReturn(
            Optional.of(mock(User.class)));

        // when & then
        assertThrows(DuplicateUserException.class, () -> joinService.join(request));
    }

    @Test
    void 이메일_인증_안됐을_때_예외_발생() {
        // given
        JoinRequest request = new JoinRequest(testEmail, testUsername, testPassword, testName,
            testGender, testBirthday);
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EMAIL_VERIFIED_KEY + request.getEmail())).willReturn("false");

        // when & then
        assertThrows(EmailNotVerifiedException.class, () -> joinService.join(request));
    }

    @Test
    void 유저네임_중복시_예외_발생() {
        // given
        JoinRequest request = new JoinRequest(testEmail, testUsername, testPassword, testName,
            testGender, testBirthday);
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EMAIL_VERIFIED_KEY + request.getEmail())).willReturn("true");
        given(userRepository.findByUsername(request.getUsername())).willReturn(
            Optional.of(mock(User.class)));

        // when & then
        assertThrows(DuplicateUserException.class, () -> joinService.join(request));
    }
}