package com.duri.domain.post.event;

import com.duri.domain.post.entity.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostCreatedEvent {

    private final Post post;

}
