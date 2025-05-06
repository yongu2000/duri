package com.duri.domain.auth.exception;


import com.duri.global.exception.BusinessException;

public class EmailNotVerifiedException extends BusinessException {

    public EmailNotVerifiedException() {
        super(AuthError.EMAIL_NOT_VERIFIED);
    }
}
