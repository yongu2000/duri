package com.duri.domain.auth.exception;


import com.duri.global.exception.BusinessException;

public class UserDetailNotFoundException extends BusinessException {

    public UserDetailNotFoundException() {
        super(AuthError.USER_DETAIL_NOT_FOUND);
    }
}
