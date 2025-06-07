package com.duri.domain.post.exception;


import com.duri.global.exception.BusinessException;

public class UnauthorizedCommentAccessException extends BusinessException {

    public UnauthorizedCommentAccessException() {
        super(CommentError.UNAUTHORIZED_COMMENT_ACCESS);
    }
}
