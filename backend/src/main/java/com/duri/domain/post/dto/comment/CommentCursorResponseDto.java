package com.duri.domain.post.dto.comment;

import com.duri.domain.post.entity.Comment;
import com.duri.global.annotation.EncryptId;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CommentCursorResponseDto {

    private LocalDateTime createdAt;
    @EncryptId
    private Long id;

    public static CommentCursorResponseDto from(Comment comment) {
        return new CommentCursorResponseDto(comment.getCreatedAt(), comment.getId());
    }
}
