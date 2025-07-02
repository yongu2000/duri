package com.duri.domain.post.dto.comment;

import com.duri.domain.post.entity.Comment;
import com.duri.global.annotation.EncryptId;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentRepliesResponseDto {

    @EncryptId
    private Long id;
    @EncryptId
    private Long parentCommentId;
    private String content;
    private String author;
    private String replyTo;
    private LocalDateTime createdAt;

    @Builder
    public CommentRepliesResponseDto(Long id, Long parentCommentId, String content, String author,
        String replyTo, LocalDateTime createdAt) {
        this.id = id;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.author = author;
        this.replyTo = replyTo;
        this.createdAt = createdAt;
    }

    public static CommentRepliesResponseDto from(Comment comment) {
        return CommentRepliesResponseDto.builder()
            .id(comment.getId())
            .parentCommentId(comment.getParentComment().getId())
            .content(comment.getContent())
            .author(comment.getCouple().getName())
            .replyTo(comment.getReplyToComment().getCouple().getName())
            .createdAt(comment.getCreatedAt())
            .build();
    }
}
