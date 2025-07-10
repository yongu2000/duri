package com.duri.domain.post.service;

import static org.mockito.BDDMockito.then;

import com.duri.domain.post.repository.PostStatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("게시글 통계 서비스 단위 테스트")
@DisplayNameGeneration(ReplaceUnderscores.class)
class PostStatServiceTest {

    @InjectMocks
    private PostStatService postStatService;

    @Mock
    private PostStatRepository postStatRepository;

    @Test
    void 좋아요_증가_정상() {
        // given
        Long postId = 1L;

        // when
        postStatService.increaseLikeCount(postId);

        // then
        then(postStatRepository).should().increaseLikeCount(postId);
    }

    @Test
    void 좋아요_감소_정상() {
        // given
        Long postId = 1L;

        // when
        postStatService.decreaseLikeCount(postId);

        // then
        then(postStatRepository).should().decreaseLikeCount(postId);
    }

}