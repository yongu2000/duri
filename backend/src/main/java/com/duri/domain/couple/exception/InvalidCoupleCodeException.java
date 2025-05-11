package com.duri.domain.couple.exception;

import com.duri.global.exception.BusinessException;

public class InvalidCoupleCodeException extends BusinessException {

    public InvalidCoupleCodeException() {
        super(CoupleError.INVALID_COUPLE_CODE);
    }

}
