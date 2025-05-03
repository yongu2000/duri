package com.duri.domain.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class VerifyCodeRequest {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    @NotBlank(message = "인증번호를 입력해주세요")
    @Pattern(regexp = "[0-9]{6}", message = "6자리 숫자 형식의 인증번호여야 합니다")
    private String code;
}
