package com.duri.domain.post.service;

import com.duri.domain.post.entity.Post;
import com.duri.domain.post.entity.PostStat;
import com.duri.domain.post.exception.PostStatNotFoundException;
import com.duri.domain.post.repository.PostStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
        postStatRepository.increaseLikeCount(postId);
    }

    public void decreaseLikeCount(Long postId) {
        postStatRepository.decreaseLikeCount(postId);
    }

    public void increaseCommentCount(Long postId) {
        postStatRepository.increaseCommentCount(postId);
    }

    public void decreaseCommentCount(Long postId) {
        postStatRepository.decreaseCommentCount(postId);
    }
}
