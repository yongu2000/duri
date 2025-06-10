package com.duri.domain.notification.exception;

import com.duri.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationError implements ErrorCode {
    UNAUTHORIZED_COMMENT_ACCESS(HttpStatus.UNAUTHORIZED, "알림에 대한 권한이 없습니다"),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다");

    private final HttpStatus status;
    private final String message;
}
