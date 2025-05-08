package com.duri.domain.couple.exception;

import com.duri.global.exception.BusinessException;

public class InvalidCoupleConnectionException extends BusinessException {

    public InvalidCoupleConnectionException() {
        super(CoupleError.INVALID_COUPLE_CONNECTION_CODE);
    }

}
