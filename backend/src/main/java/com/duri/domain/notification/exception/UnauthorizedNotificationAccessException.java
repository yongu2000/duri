package com.duri.domain.notification.exception;


import com.duri.global.exception.BusinessException;

public class UnauthorizedNotificationAccessException extends BusinessException {

    public UnauthorizedNotificationAccessException() {
        super(NotificationError.UNAUTHORIZED_COMMENT_ACCESS);
    }
}
