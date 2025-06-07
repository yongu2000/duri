package com.duri.domain.post.exception;


import com.duri.global.exception.BusinessException;

public class CommentNotFoundException extends BusinessException {

    public CommentNotFoundException() {
        super(CommentError.COMMENT_NOT_FOUND);
    }
}
