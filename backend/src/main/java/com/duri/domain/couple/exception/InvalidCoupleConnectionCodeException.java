package com.duri.domain.couple.exception;


import com.duri.global.exception.BusinessException;

public class InvalidCoupleConnectionCodeException extends BusinessException {

    public InvalidCoupleConnectionCodeException() {
        super(CoupleError.INVALID_COUPLE_CONNECTION_CODE);
    }
}
