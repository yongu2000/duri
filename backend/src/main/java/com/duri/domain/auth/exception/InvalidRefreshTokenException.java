package com.duri.domain.auth.exception;


import com.duri.global.exception.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException() {
        super(AuthError.INVALID_TOKEN);
    }
}
