package com.duri.domain.image.exception;

import com.duri.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageError implements ErrorCode {
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾지 못했습니다"),
    IMAGE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "파일 업로드가 실패했습니다");

    private final HttpStatus status;
    private final String message;

}
