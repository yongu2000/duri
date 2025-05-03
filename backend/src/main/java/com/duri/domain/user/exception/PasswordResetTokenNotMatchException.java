package com.duri.domain.user.exception;

import com.duri.global.exception.BusinessException;

public class PasswordResetTokenNotMatchException extends BusinessException {

    public PasswordResetTokenNotMatchException() {
        super(UserError.PASSWORD_RESET_TOKEN_NOT_MATCH);
    }
}
