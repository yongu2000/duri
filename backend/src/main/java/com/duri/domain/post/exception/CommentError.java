package com.duri.domain.post.exception;

import com.duri.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentError implements ErrorCode {
    UNAUTHORIZED_COMMENT_ACCESS(HttpStatus.UNAUTHORIZED, "댓글에 대한 권한이 없습니다"),
    COMMENT_STAT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글 통계입니다"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다");

    private final HttpStatus status;
    private final String message;
}
