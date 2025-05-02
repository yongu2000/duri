package com.duri.domain.auth.jwt.exception;


import com.duri.global.exception.BusinessException;

public class DuplicateUserException extends BusinessException {

    public DuplicateUserException() {
        super(AuthError.DUPLICATE_USER);
    }
}
