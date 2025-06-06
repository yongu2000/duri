package com.duri.domain.post.service;

import com.duri.domain.post.entity.Post;
import com.duri.domain.post.entity.PostStat;
import com.duri.domain.post.exception.PostStatNotFoundException;
import com.duri.domain.post.repository.PostStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostStatService {

    private final PostStatRepository postStatRepository;

    public PostStat findByPostId(Long postId) {
        return postStatRepository.findByPostId(postId).orElseThrow(PostStatNotFoundException::new);
    }

    public void create(Post post) {
        postStatRepository.save(PostStat.builder()
            .post(post)
            .build());
    }

    public void increaseLikeCount(Long postId) {
        PostStat postStat = findByPostId(postId);

        postStat.increaseLikeCount();
    }

    public void decreaseLikeCount(Long postId) {
        PostStat postStat = findByPostId(postId);

        postStat.decreaseLikeCount();
    }
}
