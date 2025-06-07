package com.duri.domain.post.dto;

import com.duri.global.util.AESUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class PostIdToken {

    private String postIdToken;

    public Long getPostId() {
        return Long.parseLong(AESUtil.decrypt(postIdToken));
    }

}
