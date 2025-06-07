package com.duri.domain.post.dto.comment;

import com.duri.domain.post.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentUpdateResponseDto {

    private String content;

    public static CommentUpdateResponseDto from(Comment comment) {
        return new CommentUpdateResponseDto(comment.getContent());
    }
}
