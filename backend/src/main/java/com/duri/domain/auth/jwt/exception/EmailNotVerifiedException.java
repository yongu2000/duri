package com.duri.domain.auth.jwt.exception;


import com.duri.global.exception.BusinessException;

public class EmailNotVerifiedException extends BusinessException {

    public EmailNotVerifiedException() {
        super(AuthError.EMAIL_NOT_VERIFIED);
    }
}
