package com.duri.domain.post.exception;


import com.duri.global.exception.BusinessException;

public class PostNotFoundException extends BusinessException {

    public PostNotFoundException() {
        super(PostError.POST_NOT_FOUND);
    }
}
