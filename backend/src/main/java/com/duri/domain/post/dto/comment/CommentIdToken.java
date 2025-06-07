package com.duri.domain.post.dto.comment;

import com.duri.global.util.AESUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CommentIdToken {

    private String commentIdToken;

    public Long getCommentId() {
        return Long.parseLong(AESUtil.decrypt(commentIdToken));
    }

}
