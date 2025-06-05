package com.duri.domain.user.service;

import static com.duri.domain.email.constant.EmailRedisKey.PASSWORD_RESET_TOKEN_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.auth.exception.UserDetailNotFoundException;
import com.duri.domain.user.dto.PasswordResetRequest;
import com.duri.domain.user.dto.UserProfileEditRequest;
import com.duri.domain.user.dto.UserResponse;
import com.duri.domain.user.entity.Gender;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.exception.PasswordResetTokenNotMatchException;
import com.duri.domain.user.exception.UserNotFoundException;
import com.duri.domain.user.repository.UserRepository;
import com.duri.global.dto.DuplicateCheckResponse;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 서비스 단위 테스트")
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserServiceTest {

    private final String testEmail = "test@example.com";
    private final String testUsername = "test";
    private final String testName = "Test User";
    private final Gender testGender = Gender.MALE;
    private final LocalDate testBirthday = LocalDate.of(2000, 4, 18);
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @InjectMocks
    private UserService userService;

    @Test
    void 이메일_중복체크_중복() {
        // given
        given(userRepository.findByEmail(testEmail))
            .willReturn(Optional.of(mock(User.class)));

        // when
        DuplicateCheckResponse response = userService.checkEmailDuplicate(testEmail);

        // then
        assertThat(response.isDuplicate()).isTrue();
    }

    @Test
    void 이메일_중복체크_중복_없음() {
        // given
        given(userRepository.findByEmail(testEmail))
            .willReturn(Optional.empty());

        // when
        DuplicateCheckResponse response = userService.checkEmailDuplicate(testEmail);

        // then
        assertThat(response.isDuplicate()).isFalse();
    }

    @Test
    void Username_중복_체크_중복() {
        // given
        given(userRepository.findByUsername(testUsername)).willReturn(
            Optional.of(mock(User.class)));

        // when
        DuplicateCheckResponse response = userService.checkUsernameDuplicate(testUsername);

        // then
        assertThat(response.isDuplicate()).isTrue();
    }

    @Test
    void Username_중복_체크_중복_없음() {
        // given
        given(userRepository.findByUsername(testUsername)).willReturn(Optional.empty());

        // when
        DuplicateCheckResponse response = userService.checkUsernameDuplicate(testUsername);

        // then
        assertThat(response.isDuplicate()).isFalse();
    }

    @Test
    void User_Id_유저_찾기_예외() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findById(1L))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 유저_정보_조회_정상() {
        // given
        User user = User.builder()
            .username(testUsername)
            .name(testName)
            .gender(testGender)
            .birthday(testBirthday)
            .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken(userDetails, null)
        );

        given(userRepository.findByUsername(testUsername)).willReturn(Optional.of(user));

        // when
        UserResponse result = userService.getUserProfile();

        // then
        assertThat(result.getUsername()).isEqualTo(testUsername);
    }

    @Test
    void 유저_정보_조회_예외() {
        // given
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("anonymousUser", null)
        );

        // when & then
        assertThatThrownBy(() -> userService.getUserProfile())
            .isInstanceOf(UserDetailNotFoundException.class);
    }

    @Test
    void 비밀번호_초기화_토큰_없음_예외() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(PASSWORD_RESET_TOKEN_KEY + "testToken")).willReturn(null);

        PasswordResetRequest request = new PasswordResetRequest("testToken", "newPassword");

        // when & then
        assertThatThrownBy(() -> userService.resetUserPassword(request))
            .isInstanceOf(PasswordResetTokenNotMatchException.class);
    }

    @Test
    void 비밀번호_초기화_정상() {
        // given
        String token = "validToken";
        String newPassword = "newPassword";
        String encodedPassword = "encodedPassword";

        PasswordResetRequest request = new PasswordResetRequest(token, newPassword);
        User mockUser = mock(User.class);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(PASSWORD_RESET_TOKEN_KEY + token)).willReturn(testEmail);
        given(userRepository.findByEmail(testEmail)).willReturn(Optional.of(mockUser));
        given(bCryptPasswordEncoder.encode(newPassword)).willReturn(encodedPassword);

        // when
        userService.resetUserPassword(request);

        // then
        then(mockUser).should().updatePassword(encodedPassword);
        then(redisTemplate).should().delete(PASSWORD_RESET_TOKEN_KEY + token);
    }

    @Test
    void 유저_정보_수정_정상() {
        // given
        User user = mock(User.class);
        given(userRepository.findByUsername(testUsername)).willReturn(Optional.of(user));
        String changedProfileImageUrl = "profile.jpg";
        String changedName = "changedName";
        Gender changedGender = Gender.FEMALE;
        LocalDate changedBirthday = LocalDate.of(1999, 5, 29);

        UserProfileEditRequest request = new UserProfileEditRequest(
            changedProfileImageUrl, changedName, changedGender, changedBirthday
        );

        // when
        userService.editUserProfile(testUsername, request);

        // then
        then(user).should().updateName(changedName);
        then(user).should().updateGender(changedGender);
        then(user).should().updateBirthday(changedBirthday);
        then(user).should().updateProfileImageUrl(changedProfileImageUrl);
    }
}