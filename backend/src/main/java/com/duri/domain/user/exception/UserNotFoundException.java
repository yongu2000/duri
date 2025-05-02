package com.duri.domain.user.exception;


import com.duri.global.exception.BusinessException;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(UserError.USER_NOT_FOUND);
    }
}
