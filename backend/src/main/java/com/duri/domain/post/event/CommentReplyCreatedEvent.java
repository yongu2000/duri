package com.duri.domain.post.event;

import com.duri.domain.post.entity.Comment;
import com.duri.domain.post.entity.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentReplyCreatedEvent {

    private final Comment comment;
    private final Comment parentComment;
    private final Comment replyTo;
    private final Post post;

}
