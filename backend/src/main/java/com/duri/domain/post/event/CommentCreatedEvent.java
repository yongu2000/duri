package com.duri.domain.post.event;

import com.duri.domain.post.entity.Comment;
import com.duri.domain.post.entity.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentCreatedEvent {

    private final Comment comment;
    private final Post post;

}
