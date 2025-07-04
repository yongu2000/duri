package com.duri.domain.auth.jwt.dto;


import com.duri.domain.user.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요")
//    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
//        message = "비밀번호는 8자 이상의 영문자, 숫자, 특수문자를 포함해야 합니다")
    private String password;

    private String name;
    private Gender gender;
    private LocalDate birthday;
}

