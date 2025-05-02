package com.duri.domain.auth.jwt.exception;


import com.duri.global.exception.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException() {
        super(AuthError.INVALID_TOKEN);
    }
}
