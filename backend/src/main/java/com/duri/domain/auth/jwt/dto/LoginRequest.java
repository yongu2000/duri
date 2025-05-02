package com.duri.domain.auth.jwt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

    private String emailOrUsername;
    private String password;
    private boolean rememberMe;
    
}
