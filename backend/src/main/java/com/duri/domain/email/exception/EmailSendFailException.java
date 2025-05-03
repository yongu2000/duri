package com.duri.domain.email.exception;


import com.duri.global.exception.BusinessException;

public class EmailSendFailException extends BusinessException {

    public EmailSendFailException() {
        super(EmailError.EMAIL_SEND_FAIL);
    }
}
