package com.duri.domain.post.exception;

import com.duri.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostError implements ErrorCode {
    DUPLICATE_LIKE_POST(HttpStatus.CONFLICT, "이미 좋아요한 게시글입니다"),
    POST_STAT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글 통계입니다"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다");

    private final HttpStatus status;
    private final String message;
}
