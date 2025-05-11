package com.duri.domain.user.exception;


import com.duri.global.exception.BusinessException;

public class InvalidGenderException extends BusinessException {

    public InvalidGenderException() {
        super(UserError.INVALID_GENDER);
    }
}
