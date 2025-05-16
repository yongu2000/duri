package com.duri.domain.post.dto;

import com.duri.domain.post.entity.Post;
import com.duri.global.util.AESUtil;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostCursor {

    private LocalDate date;
    private Integer rate;
    private String idToken;

    public static PostCursor from(Post post) {
        try {
            return new PostCursor(post.getDate(), post.getRate(),
                AESUtil.encrypt(post.getId().toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long getId() {
        try {
            return Long.parseLong(AESUtil.decrypt(idToken));
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 커서입니다");
        }
    }

}
