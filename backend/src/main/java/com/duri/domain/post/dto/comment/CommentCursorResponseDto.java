package com.duri.domain.post.dto.comment;

import com.duri.domain.post.entity.Comment;
import com.duri.global.util.AESUtil;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CommentCursorResponseDto {


    private LocalDateTime createdAt;
    private String id;

    public static CommentCursorResponseDto from(
        Comment comment) {
        try {
            return new CommentCursorResponseDto(
                comment.getCreatedAt(),
                AESUtil.encrypt(String.valueOf(comment.getId()))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
