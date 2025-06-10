package com.duri.domain.post.event;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.post.entity.Post;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostLikedEvent {

    private final Post post;
    private final Couple couple;

}
