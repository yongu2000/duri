package com.duri.domain.post.service;

import com.duri.domain.post.entity.PostStat;
import com.duri.domain.post.exception.PostStatNotFoundException;
import com.duri.domain.post.repository.PostStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostStatService {

    private final PostStatRepository postStatRepository;

    public void increaseLike(Long postId) {
        PostStat postStat = postStatRepository.findByPostId(postId)
            .orElseThrow(PostStatNotFoundException::new);

        postStat.increaseLike();
    }

    public void decreaseLike(Long postId) {
        PostStat postStat = postStatRepository.findByPostId(postId)
            .orElseThrow(PostStatNotFoundException::new);

        postStat.decreaseLike();
    }
}
