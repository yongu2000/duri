package com.duri.domain.post.exception;


import com.duri.global.exception.BusinessException;

public class DuplicateLikePostException extends BusinessException {

    public DuplicateLikePostException() {
        super(PostError.DUPLICATE_LIKE_POST);
    }
}
