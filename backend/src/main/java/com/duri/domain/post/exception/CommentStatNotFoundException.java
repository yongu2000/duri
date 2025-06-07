package com.duri.domain.post.exception;


import com.duri.global.exception.BusinessException;

public class CommentStatNotFoundException extends BusinessException {

    public CommentStatNotFoundException() {
        super(CommentError.COMMENT_STAT_NOT_FOUND);
    }
}
