package com.duri.domain.post.dto;

import com.duri.global.util.AESUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PostImageRequest {

    private String postIdToken;

    public Long getPostId() {
        return Long.parseLong(AESUtil.decrypt(postIdToken));
    }
}
