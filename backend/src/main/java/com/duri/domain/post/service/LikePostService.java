package com.duri.domain.post.service;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.service.CoupleService;
import com.duri.domain.post.dto.PostLikeStatusResponseDto;
import com.duri.domain.post.entity.LikePost;
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.exception.DuplicateLikePostException;
import com.duri.domain.post.exception.PostNotFoundException;
import com.duri.domain.post.repository.LikePostRepository;
import com.duri.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikePostService {

    private final LikePostRepository likePostRepository;
    private final PostStatService postStatService;
    private final PostRepository postRepository;
    private final CoupleService coupleService;

    @Transactional
    public void like(String coupleCode, Long postId) {
        Couple couple = coupleService.findByCode(coupleCode);

        if (likePostRepository.findByCoupleIdAndPostId(couple.getId(), postId).isPresent()) {
            throw new DuplicateLikePostException();
        }
        Post post = postRepository.findById(postId)
            .orElseThrow(PostNotFoundException::new);
        likePostRepository.save(LikePost.builder()
            .couple(couple)
            .post(post)
            .build());

        // 이벤트 or ASYNC 처리?
        postStatService.increaseLikeCount(post.getId());
        // 좋아요 눌렀다는 알림 보내기
    }

    @Transactional
    public void dislike(String coupleCode, Long postId) {
        Couple couple = coupleService.findByCode(coupleCode);

        LikePost likePost = likePostRepository.findByCoupleIdAndPostId(couple.getId(), postId)
            .orElseThrow(PostNotFoundException::new);
        Post post = postRepository.findById(postId)
            .orElseThrow(PostNotFoundException::new);
        likePostRepository.delete(likePost);

        // 이벤트 or ASYNC 처리?
        postStatService.decreaseLikeCount(post.getId());
    }

    @Transactional(readOnly = true)
    public PostLikeStatusResponseDto getLikeStatus(String coupleCode, Long postId) {
        Couple couple = coupleService.findByCode(coupleCode);

        boolean liked = likePostRepository.findByCoupleIdAndPostId(couple.getId(), postId)
            .isPresent();
        return new PostLikeStatusResponseDto(liked);
    }
}
