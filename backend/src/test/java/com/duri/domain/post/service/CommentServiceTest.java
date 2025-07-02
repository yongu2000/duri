package com.duri.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.service.CoupleService;
import com.duri.domain.post.constant.search.CommentSortBy;
import com.duri.domain.post.constant.search.SortDirection;
import com.duri.domain.post.dto.comment.CommentCreateRequestDto;
import com.duri.domain.post.dto.comment.CommentCursorRequestDto;
import com.duri.domain.post.dto.comment.CommentCursorResponseDto;
import com.duri.domain.post.dto.comment.CommentRepliesResponseDto;
import com.duri.domain.post.dto.comment.CommentReplyCreateRequestDto;
import com.duri.domain.post.dto.comment.CommentSearchOptions;
import com.duri.domain.post.dto.comment.CommentUpdateRequestDto;
import com.duri.domain.post.dto.comment.CommentUpdateResponseDto;
import com.duri.domain.post.dto.comment.ParentCommentResponseDto;
import com.duri.domain.post.entity.Comment;
import com.duri.domain.post.entity.CommentStat;
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.exception.CommentNotFoundException;
import com.duri.domain.post.repository.comment.CommentRepository;
import com.duri.global.dto.CursorResponse;
import com.duri.global.util.AESUtilTestHelper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("댓글 서비스 단위 테스트")
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentStatService commentStatService;

    @Mock
    private CoupleService coupleService;

    @Mock
    private PostService postService;

    @Mock
    private PostStatService postStatService;

    @Test
    void 댓글_생성_성공() {
        // given
        String coupleCode = "ABC123";
        Long postId = 1L;
        String content = "댓글 내용";

        Couple couple = Couple.builder().id(1L).name("테스트커플").build();
        Post post = Post.builder().id(postId).title("테스트 게시글").build();

        CommentCreateRequestDto request = new CommentCreateRequestDto(content);

        given(coupleService.findByCode(coupleCode)).willReturn(couple);
        given(postService.findById(postId)).willReturn(post);
        given(commentRepository.save(any(Comment.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // when
        commentService.create(coupleCode, postId, request);

        // then
        then(commentRepository).should().save(any(Comment.class));
        then(commentStatService).should().create(any(Comment.class));

    }

    @Test
    void 댓글_ID_조회_성공() {
        // given
        Long commentId = 1L;
        Comment comment = mock(Comment.class);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        Comment result = commentService.findById(commentId);

        // then
        assertThat(result).isEqualTo(comment);
    }

    // findById 실패
    @Test
    void 댓글_ID_조회_실패_예외발생() {
        // given
        Long commentId = 999L;
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.findById(commentId))
            .isInstanceOf(CommentNotFoundException.class);
    }

    // update는 권한 어노테이션 때문에 통합 테스트 또는 AspectTest 필요
    @Test
    void 댓글_수정_성공() {
        // given
        Long commentId = 1L;
        String newContent = "수정된 내용";
        Comment comment = mock(Comment.class);

        CommentUpdateRequestDto request = new CommentUpdateRequestDto(newContent);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        CommentUpdateResponseDto response = commentService.update(commentId, request);

        // then
        then(comment).should().update(newContent);
        assertThat(response).isNotNull();
    }

    @Test
    void 댓글_삭제_성공() {
        // given
        Long commentId = 1L;
        Post post = Post.builder().id(10L).build();
        Comment comment = Comment.builder().id(commentId).post(post).build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.delete(commentId);

        // then
        then(postStatService).should().decreaseCommentCount(post.getId());
        then(commentRepository).should().delete(comment);
    }

    @Test
    void 게시글의_댓글_목록_조회() {
        // given
        Long postId = 1L;

        Couple couple = Couple.builder()
            .id(1L)
            .name("테스트커플")
            .build();

        Comment comment1 = Comment.builder()
            .id(1L)
            .content("첫번째 댓글")
            .couple(couple)
            .commentStat(CommentStat.builder()
                .build())
            .build();

        Comment comment2 = Comment.builder()
            .id(2L)
            .content("두번째 댓글")
            .couple(couple)
            .commentStat(CommentStat.builder()
                .build())
            .build();

        CommentCursorRequestDto cursor = new CommentCursorRequestDto(null, null);
        CommentSearchOptions searchOptions = new CommentSearchOptions(CommentSortBy.CREATED_AT,
            SortDirection.DESC);

        given(commentRepository.findParentCommentsByPost(cursor, 3, searchOptions,
            postId)).willReturn(
            List.of(comment1, comment2));

        // when
        CursorResponse<ParentCommentResponseDto, CommentCursorResponseDto> parentComments = commentService.getParentComments(
            cursor, 2,
            searchOptions,
            postId);

        // then
        assertThat(parentComments.getItems()).hasSize(2);
    }

    @Test
    void 대댓글_생성_성공() {
        // given
        String coupleCode = "ABC123";
        Long parentCommentId = 1L;
        String replyContent = "대댓글 내용";

        Couple couple = Couple.builder().id(1L).name("테스트커플").build();
        Post post = Post.builder().id(10L).title("테스트 게시글").build();
        Comment parentComment = Comment.builder()
            .id(parentCommentId)
            .content("부모 댓글")
            .couple(couple)
            .post(post)
            .build();

        CommentReplyCreateRequestDto request = new CommentReplyCreateRequestDto(replyContent);

        given(coupleService.findByCode(coupleCode)).willReturn(couple);
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));

        // when
        commentService.createReply(coupleCode, parentCommentId, request);

        // then
        then(commentRepository).should().save(any(Comment.class));
        then(postStatService).should().increaseCommentCount(post.getId());
        then(commentStatService).should().increaseCommentCount(parentComment.getId());
    }

    @Test
    void 대댓글_생성_실패_댓글없음() {
        // given
        String coupleCode = "ABC123";
        Long invalidCommentId = 999L;
        CommentReplyCreateRequestDto request = new CommentReplyCreateRequestDto("대댓글");

        given(commentRepository.findById(invalidCommentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createReply(coupleCode, invalidCommentId, request))
            .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void 대댓글_목록_조회_성공() {
        // given
        Long parentCommentId = 1L;

        Couple couple = Couple.builder()
            .id(1L)
            .name("테스트커플")
            .build();

        Comment parent = Comment.builder()
            .id(parentCommentId)
            .content("부모 댓글")
            .couple(couple)
            .build();

        Comment reply1 = Comment.builder()
            .id(2L)
            .content("첫 번째 대댓글")
            .couple(couple)
            .parentComment(parent)
            .replyToComment(parent)
            .build();

        Comment reply2 = Comment.builder()
            .id(3L)
            .content("두 번째 대댓글")
            .couple(couple)
            .parentComment(parent)
            .replyToComment(parent)
            .build();

        CommentCursorRequestDto cursor = new CommentCursorRequestDto(null, null);

        given(commentRepository.findCommentRepliesByComment(cursor, 3,
            parentCommentId))
            .willReturn(List.of(CommentRepliesResponseDto.from(reply1),
                CommentRepliesResponseDto.from(reply2)));

        // when
        CursorResponse<CommentRepliesResponseDto, CommentCursorResponseDto> result = commentService.getCommentReplies(
            cursor, 2,
            parentCommentId);

        // then
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getContent()).isEqualTo("첫 번째 대댓글");
        assertThat(result.getItems().get(1).getContent()).isEqualTo("두 번째 대댓글");

        then(commentRepository).should().findCommentRepliesByComment(cursor, 3,
            parentCommentId);
    }

    @BeforeEach
    void setUp() {
        AESUtilTestHelper.setSecretKey("1234567890abcdef");
    }

}