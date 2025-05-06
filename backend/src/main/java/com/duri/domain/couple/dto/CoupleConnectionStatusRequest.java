package com.duri.domain.couple.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CoupleConnectionStatusRequest {

    @NotBlank(message = "인증코드를 입력해주세요")
    @Pattern(regexp = "^[A-Z0-9]{8}$", message = "8자리 대문자+숫자 형식의 코드여야 합니다")
    private String code;
}
