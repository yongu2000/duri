package com.duri.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordResetRequest {

    @NotBlank(message = "토큰을 포함해주세요")
    private String token;
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String newPassword;

}
