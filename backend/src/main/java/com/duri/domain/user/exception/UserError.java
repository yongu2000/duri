package com.duri.domain.user.exception;

import com.duri.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserError implements ErrorCode {
    PASSWORD_RESET_TOKEN_NOT_MATCH(HttpStatus.UNAUTHORIZED, "비밀번호 초기화 토큰이 일치하지 않습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다");

    private final HttpStatus status;
    private final String message;
}
