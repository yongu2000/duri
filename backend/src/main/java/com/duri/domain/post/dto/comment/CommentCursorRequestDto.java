package com.duri.domain.post.dto.comment;

import com.duri.global.annotation.DecryptId;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CommentCursorRequestDto {

    private LocalDateTime createdAt;
    @DecryptId
    private Long id;

}
