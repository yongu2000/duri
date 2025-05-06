package com.duri.domain.couple.exception;

import com.duri.global.exception.BusinessException;

public class ExistingCoupleConnectionException extends BusinessException {

    public ExistingCoupleConnectionException() {
        super(CoupleError.EXISTING_COUPLE_CONNECTION);
    }
}

