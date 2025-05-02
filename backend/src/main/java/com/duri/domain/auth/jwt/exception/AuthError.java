package com.duri.domain.auth.jwt.exception;

import com.duri.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthError implements ErrorCode {

    USER_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "유효한 인증 정보가 없습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다"),
    DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 유저입니다");

    private final HttpStatus status;
    private final String message;
}
