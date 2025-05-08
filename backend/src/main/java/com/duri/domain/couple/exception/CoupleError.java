package com.duri.domain.couple.exception;

import com.duri.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum CoupleError implements ErrorCode {
    INVALID_COUPLE_CONNECTION(HttpStatus.BAD_REQUEST, "유효하지 않은 커플 연결 요청입니다"),
    EXISTING_COUPLE_CONNECTION(HttpStatus.CONFLICT, "이미 존재하는 커플 연결 요청입니다"),
    INVALID_COUPLE_CONNECTION_CODE(HttpStatus.NOT_FOUND, "올바르지 않은 인증코드 입니다");

    private final HttpStatus status;
    private final String message;
}
