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
public class ParentCommentResponseDto {

    private String id;
    private String content;
    private String author;
    private Long commentCount;
    private LocalDateTime createdAt;

    public static ParentCommentResponseDto from(Comment comment) {
        return ParentCommentResponseDto.builder()
            .id(AESUtil.encrypt(String.valueOf(comment.getId())))
            .content(comment.getContent())
            .author(comment.getCouple().getName())
            .commentCount(comment.getCommentStat().getCommentCount())
            .createdAt(comment.getCreatedAt())
            .build();
    }
}
