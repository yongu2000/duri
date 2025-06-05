package com.duri.domain.auth.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    private String emailOrUsername;
    private String password;
    private boolean rememberMe;

}
