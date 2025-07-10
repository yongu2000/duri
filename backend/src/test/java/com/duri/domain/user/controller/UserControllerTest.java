package com.duri.domain.user.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.user.dto.PasswordResetRequest;
import com.duri.domain.user.dto.UserProfileEditRequest;
import com.duri.domain.user.dto.UserResponse;
import com.duri.domain.user.entity.Gender;
import com.duri.domain.user.entity.Position;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.service.UserService;
import com.duri.global.dto.DuplicateCheckResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@DisplayName("사용자 컨트롤러 단위 테스트")
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private final String testEmail = "test@example.com";
    private final String testUsername = "test";
    private final String testName = "Test User";
    private final String testCoupleCode = "testCoupleCode";
    private final String testProfileImageUrl = "image.jpg";
    private final Gender testGender = Gender.MALE;
    private final Position testPosition = Position.LEFT;
    private final LocalDateTime testCreatedAt = LocalDateTime.of(2025, 5, 1, 12, 30);
    private final LocalDate testBirthday = LocalDate.of(2000, 4, 18);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void clearSecurityContextHolder() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("/user/my - 유저 정보 가져오기")
    void getUserProfile() throws Exception {
        // given
        User user = User.builder()
            .email(testEmail)
            .username(testUsername)
            .name(testName)
            .coupleCode(testCoupleCode)
            .profileImageUrl(testProfileImageUrl)
            .birthday(testBirthday)
            .gender(testGender)
            .position(testPosition)
            .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
        UserResponse response = UserResponse.from(user);
        given(userService.getUserProfile()).willReturn(response);

        // expect
        mockMvc.perform(get("/user/my"))
            .andExpect(status().isOk())
            .andDo(document("user-get-profile",
                responseFields(
                    fieldWithPath("email").description("유저 ID"),
                    fieldWithPath("username").description("유저 아이디"),
                    fieldWithPath("name").description("이름"),
                    fieldWithPath("coupleCode").description("커플 코드"),
                    fieldWithPath("profileImageUrl").description("프로필 이미지 URL"),
                    fieldWithPath("birthday").description("생일"),
                    fieldWithPath("gender").description("성별"),
                    fieldWithPath("position").description("표시 위치"),
                    fieldWithPath("createdAt").description("가입일")
                )
            ));
    }

    @Test
    @DisplayName("/user/check/email/{email} - 이메일 중복 체크")
    void checkEmailDuplicate() throws Exception {
        // given
        given(userService.checkEmailDuplicate(testEmail))
            .willReturn(new DuplicateCheckResponse(false));

        // expect
        mockMvc.perform(get("/user/check/email/{email}", testEmail))
            .andExpect(status().isOk())
            .andDo(document("user-check-email",
                pathParameters(
                    parameterWithName("email").description("중복 확인할 이메일")
                ),
                responseFields(
                    fieldWithPath("isDuplicate").description("이메일 중복 여부 (true: 중복, false: 사용 가능)")
                )
            ));
    }

    @Test
    @DisplayName("/user/check/username/{username} - 유저네임 중복 체크")
    void checkUsernameDuplicate() throws Exception {
        // given
        given(userService.checkUsernameDuplicate(testUsername))
            .willReturn(new DuplicateCheckResponse(true));

        // expect
        mockMvc.perform(get("/user/check/username/{username}", testUsername))
            .andExpect(status().isOk())
            .andDo(document("user-check-username",
                pathParameters(
                    parameterWithName("username").description("중복 확인할 유저네임")
                ),
                responseFields(
                    fieldWithPath("isDuplicate").description("유저네임 중복 여부 (true: 중복, false: 사용 가능)")
                )
            ));
    }

    @Test
    @DisplayName("/user/profile/{username}/edit - 유저 프로필 수정")
    void editUserProfile() throws Exception {
        UserProfileEditRequest request = new UserProfileEditRequest(testProfileImageUrl, testName,
            testGender, testBirthday);

        mockMvc.perform(put("/user/profile/{username}/edit", testUsername)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(document("user-edit-profile",
                pathParameters(
                    parameterWithName("username").description("수정할 유저네임")
                ),
                requestFields(
                    fieldWithPath("profileImageUrl").description("프로필 이미지 URL"),
                    fieldWithPath("name").description("이름"),
                    fieldWithPath("gender").description("성별"),
                    fieldWithPath("birthday").description("생일")
                )
            ));
    }

    @Test
    @DisplayName("/user/password/reset - 비밀번호 재설정")
    void resetUserPassword() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
            "PasswordResetToken", "newPassword"
        );

        mockMvc.perform(post("/user/password/reset")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(document("user-password-reset",
                requestFields(
                    fieldWithPath("token").description("비밀번호를 재설정용 발급받은 토큰"),
                    fieldWithPath("newPassword").description("새 비밀번호")
                )
            ));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new ParameterNamesModule());
            objectMapper.registerModule(new Jdk8Module());
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper;
        }
    }
}