package com.duri.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.service.CoupleService;
import com.duri.domain.post.dto.PostLikeStatusResponseDto;
import com.duri.domain.post.entity.LikePost;
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.exception.DuplicateLikePostException;
import com.duri.domain.post.exception.PostNotFoundException;
import com.duri.domain.post.repository.LikePostRepository;
import com.duri.domain.post.repository.PostRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("글 좋아요 서비스 단위 테스트")
@DisplayNameGeneration(ReplaceUnderscores.class)
class LikePostServiceTest {

    private final String coupleCode = "testCoupleCode";
    private final Long postId = 1L;
    private final Couple couple = Couple.builder().id(10L).build();
    private final Post post = Post.builder().id(postId).build();
    @InjectMocks
    private LikePostService likePostService;
    @Mock
    private LikePostRepository likePostRepository;
    @Mock
    private PostStatService postStatService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CoupleService coupleService;

    @Test
    void 좋아요_정상() {
        // given
        given(coupleService.findByCode(coupleCode)).willReturn(couple);
        given(likePostRepository.findByCoupleIdAndPostId(couple.getId(), postId)).willReturn(
            Optional.empty());
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        likePostService.like(coupleCode, postId);

        // then
        then(likePostRepository).should().save(any(LikePost.class));
        then(postStatService).should().increaseLikeCount(postId);
    }

    @Test
    void 좋아요_중복_예외() {
        // given
        given(coupleService.findByCode(coupleCode)).willReturn(couple);
        given(likePostRepository.findByCoupleIdAndPostId(couple.getId(), postId))
            .willReturn(Optional.of(mock(LikePost.class)));

        // when & then
        assertThatThrownBy(() -> likePostService.like(coupleCode, postId)).isInstanceOf(
            DuplicateLikePostException.class);
    }

    @Test
    void 좋아요_게시글_없음_PostNotFoundException() {
        // given
        given(coupleService.findByCode(coupleCode)).willReturn(couple);
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likePostService.like(coupleCode, postId))
            .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void 좋아요_취소_정상() {
        // given
        LikePost likePost = LikePost.builder().id(100L).couple(couple).post(post).build();

        given(coupleService.findByCode(coupleCode)).willReturn(couple);
        given(likePostRepository.findByCoupleIdAndPostId(couple.getId(), postId)).willReturn(
            Optional.of(likePost));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        likePostService.dislike(coupleCode, postId);

        // then
        then(likePostRepository).should().delete(likePost);
        then(postStatService).should().decreaseLikeCount(postId);
    }

    @Test
    void 좋아요_취소_좋아요_안돼있음_예외() {
        // given
        given(coupleService.findByCode(coupleCode)).willReturn(couple);
        given(likePostRepository.findByCoupleIdAndPostId(couple.getId(), postId)).willReturn(
            Optional.empty());

        // when & then
        assertThatThrownBy(() -> likePostService.dislike(coupleCode, postId)).isInstanceOf(
            PostNotFoundException.class);
    }

    @Test
    void 좋아요_되어_있음_정상() {
        // given
        given(coupleService.findByCode(coupleCode)).willReturn(couple);
        given(likePostRepository.findByCoupleIdAndPostId(couple.getId(), postId))
            .willReturn(Optional.of(mock(LikePost.class)));

        // when
        PostLikeStatusResponseDto result = likePostService.getLikeStatus(coupleCode, postId);

        // then
        assertThat(result.isLiked()).isEqualTo(true);
    }

    @Test
    void 좋아요_안돼_있음_정상() {
        // given
        given(coupleService.findByCode(coupleCode)).willReturn(couple);
        given(likePostRepository.findByCoupleIdAndPostId(couple.getId(), postId))
            .willReturn(Optional.empty());

        // when
        PostLikeStatusResponseDto result = likePostService.getLikeStatus(coupleCode, postId);

        // then
        assertThat(result.isLiked()).isEqualTo(false);
    }
}