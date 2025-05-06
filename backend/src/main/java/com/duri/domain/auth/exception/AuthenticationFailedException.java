package com.duri.domain.auth.exception;

import com.duri.global.exception.BusinessException;

public class AuthenticationFailedException extends BusinessException {

    public AuthenticationFailedException() {
        super(AuthError.AUTHENTICATION_FAILED);
    }
}
