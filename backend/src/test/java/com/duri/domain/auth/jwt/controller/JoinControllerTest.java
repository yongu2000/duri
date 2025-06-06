package com.duri.domain.auth.jwt.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duri.domain.auth.exception.AuthError;
import com.duri.domain.auth.exception.DuplicateUserException;
import com.duri.domain.auth.exception.EmailNotVerifiedException;
import com.duri.domain.auth.jwt.dto.JoinRequest;
import com.duri.domain.auth.jwt.dto.JoinResponse;
import com.duri.domain.auth.jwt.service.JoinService;
import com.duri.domain.user.entity.Gender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@WebMvcTest(JoinController.class)
@DisplayName("유저 회원가입 Controller")
@AutoConfigureMockMvc(addFilters = false)
class JoinControllerTest {

    private static final Long testId = 1L;
    private final String testEmail = "test@example.com";
    private final String testUsername = "test";
    private final String testPassword = "test password";
    private final String testName = "Test User";
    private final Gender testGender = Gender.MALE;
    private final LocalDate testBirthday = LocalDate.of(2000, 4, 18);

    @MockitoBean
    private JoinService joinService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("/join - 회원가입 성공")
    void 회원가입_성공_정상_회원가입() throws Exception {
        // given
        JoinRequest request = new JoinRequest(testEmail, testUsername, testPassword, testName,
            testGender, testBirthday);
        JoinResponse response = JoinResponse.builder()
            .id(testId)
            .username(testUsername)
            .build();
        given(joinService.join(any(JoinRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(testId))
            .andExpect(jsonPath("$.username").value(testUsername))
            .andDo(document("join-success",
                requestFields(
                    fieldWithPath("email").description("이메일"),
                    fieldWithPath("username").description("사용자 이름"),
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("name").description("이름"),
                    fieldWithPath("gender").description("성별"),
                    fieldWithPath("birthday").description("생년월일")
                ),
                responseFields(
                    fieldWithPath("id").description("유저 ID"),
                    fieldWithPath("username").description("사용자 이름")
                )
            ));
    }


    @Test
    @DisplayName("/join - 회원가입 실패 중복된 이메일/Username")
    void 회원가입_실패_중복된_이메일() throws Exception {
        // given
        JoinRequest request = new JoinRequest(
            testEmail, testUsername, testPassword, testName, testGender, testBirthday
        );

        given(joinService.join(any(JoinRequest.class)))
            .willThrow(new DuplicateUserException());

        // when & then
        mockMvc.perform(post("/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(AuthError.DUPLICATE_USER.getMessage()))
            .andExpect(jsonPath("$.code").value(AuthError.DUPLICATE_USER.name()))
            .andExpect(jsonPath("$.status").value(AuthError.DUPLICATE_USER.getStatus().value()))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.details").isMap())
            .andDo(document("join-failure",
                requestFields(
                    fieldWithPath("email").description("이메일"),
                    fieldWithPath("username").description("사용자명"),
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("name").description("이름"),
                    fieldWithPath("gender").description("성별"),
                    fieldWithPath("birthday").description("생년월일")
                ),
                responseFields(
                    fieldWithPath("message").description("에러 메시지"),
                    fieldWithPath("status").description("HTTP 상태 코드"),
                    fieldWithPath("code").description("에러 코드"),
                    fieldWithPath("timestamp").description("에러 발생 시각"),
                    fieldWithPath("details").description("추가 에러 정보")
                )
            ));
    }

    @Test
    @DisplayName("/join - 회원가입 실패 이메일 인증 안됨")
    void 회원가입_실패_이메일_인증_안됨() throws Exception {
        // given
        JoinRequest request = new JoinRequest(
            testEmail, testUsername, testPassword, testName, testGender, testBirthday
        );

        given(joinService.join(any(JoinRequest.class)))
            .willThrow(new EmailNotVerifiedException());

        // when & then
        mockMvc.perform(post("/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(AuthError.EMAIL_NOT_VERIFIED.getMessage()))
            .andExpect(jsonPath("$.code").value(AuthError.EMAIL_NOT_VERIFIED.name()))
            .andExpect(jsonPath("$.status").value(AuthError.EMAIL_NOT_VERIFIED.getStatus().value()))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.details").isMap())
            .andDo(document("join-failure",
                requestFields(
                    fieldWithPath("email").description("이메일"),
                    fieldWithPath("username").description("사용자명"),
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("name").description("이름"),
                    fieldWithPath("gender").description("성별"),
                    fieldWithPath("birthday").description("생년월일")
                ),
                responseFields(
                    fieldWithPath("message").description("에러 메시지"),
                    fieldWithPath("status").description("HTTP 상태 코드"),
                    fieldWithPath("code").description("에러 코드"),
                    fieldWithPath("timestamp").description("에러 발생 시각"),
                    fieldWithPath("details").description("추가 에러 정보")
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