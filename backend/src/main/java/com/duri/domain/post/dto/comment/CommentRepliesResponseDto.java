package com.duri.domain.post.dto.comment;

import com.duri.domain.post.entity.Comment;
import com.duri.global.util.AESUtil;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CommentRepliesResponseDto {

    private String commentIdToken;
    private String parentCommentIdToken;
    private String content;
    private String author;
    private String replyTo;
    private LocalDateTime createdAt;

    public static CommentRepliesResponseDto from(Comment comment) {
        return CommentRepliesResponseDto.builder()
            .commentIdToken(AESUtil.encrypt(String.valueOf(comment.getId())))
            .parentCommentIdToken(
                AESUtil.encrypt(String.valueOf(comment.getParentComment().getId())))
            .content(comment.getContent())
            .author(comment.getCouple().getName())
            .replyTo(comment.getReplyToComment().getCouple().getName())
            .createdAt(comment.getCreatedAt())
            .build();
    }
}
