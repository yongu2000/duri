package com.duri.domain.user.exception;

import com.duri.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserError implements ErrorCode {
    
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다");

    private final HttpStatus status;
    private final String message;
}
