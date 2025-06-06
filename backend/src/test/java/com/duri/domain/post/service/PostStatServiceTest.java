package com.duri.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.duri.domain.post.entity.PostStat;
import com.duri.domain.post.exception.PostStatNotFoundException;
import com.duri.domain.post.repository.PostStatRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostStatServiceTest {

    @InjectMocks
    private PostStatService postStatService;

    @Mock
    private PostStatRepository postStatRepository;

    @Test
    void 좋아요_증가_정상() {
        // given
        Long postId = 1L;
        PostStat postStat = PostStat.builder().likeCount(0L).build();
        given(postStatRepository.findByPostId(postId)).willReturn(Optional.of(postStat));

        // when
        postStatService.increaseLikeCount(postId);

        // then
        then(postStatRepository).should().findByPostId(postId);
        assertThat(postStat.getLikeCount()).isEqualTo(1L);
    }

    @Test
    void 좋아요_증가_postStat_없음_예외() {
        // given
        Long postId = 1L;
        given(postStatRepository.findByPostId(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postStatService.increaseLikeCount(postId))
            .isInstanceOf(PostStatNotFoundException.class);
    }

    @Test
    void 좋아요_감소_정상() {
        // given
        Long postId = 2L;
        PostStat postStat = PostStat.builder().likeCount(3L).build();
        given(postStatRepository.findByPostId(postId)).willReturn(Optional.of(postStat));

        // when
        postStatService.decreaseLikeCount(postId);

        // then
        then(postStatRepository).should().findByPostId(postId);
        assertThat(postStat.getLikeCount()).isEqualTo(2L);
    }

    @Test
    void 좋아요_감소_postStat_없음_예외() {
        // given
        Long postId = 2L;
        given(postStatRepository.findByPostId(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postStatService.decreaseLikeCount(postId))
            .isInstanceOf(PostStatNotFoundException.class);
    }
}