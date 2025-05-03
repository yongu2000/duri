package com.duri.domain.email.exception;

import com.duri.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum EmailError implements ErrorCode {

    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다");


    private final HttpStatus status;
    private final String message;
}
