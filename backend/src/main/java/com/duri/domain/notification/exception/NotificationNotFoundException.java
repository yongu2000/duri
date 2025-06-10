package com.duri.domain.notification.exception;


import com.duri.global.exception.BusinessException;

public class NotificationNotFoundException extends BusinessException {

    public NotificationNotFoundException() {
        super(NotificationError.NOTIFICATION_NOT_FOUND);
    }
}
