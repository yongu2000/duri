package com.duri.domain.post.exception;


import com.duri.global.exception.BusinessException;

public class PostStatNotFoundException extends BusinessException {

    public PostStatNotFoundException() {
        super(PostError.POST_STAT_NOT_FOUND);
    }
}
