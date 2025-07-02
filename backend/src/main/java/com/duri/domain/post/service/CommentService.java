package com.duri.domain.post.service;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.service.CoupleService;
import com.duri.domain.post.annotation.CheckCommentPermission;
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
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.event.CommentCreatedEvent;
import com.duri.domain.post.event.CommentReplyCreatedEvent;
import com.duri.domain.post.exception.CommentNotFoundException;
import com.duri.domain.post.repository.comment.CommentRepository;
import com.duri.global.dto.CursorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final CommentRepository commentRepository;
    private final CommentStatService commentStatService;
    private final CoupleService coupleService;
    private final PostService postService;
    private final PostStatService postStatService;

    public void create(String coupleCode, Long postId, CommentCreateRequestDto request) {
        Couple couple = coupleService.findByCode(coupleCode);
        Post post = postService.findById(postId);

        Comment comment = commentRepository.save(Comment.builder()
            .content(request.getContent())
            .couple(couple)
            .post(post)
            .build());
        commentStatService.create(comment);

        applicationEventPublisher.publishEvent(new CommentCreatedEvent(comment, post));
    }

    public void createReply(String coupleCode, Long commentId,
        CommentReplyCreateRequestDto request) {
        Couple couple = coupleService.findByCode(coupleCode);
        Comment replyTo = findById(commentId);
        Post post = replyTo.getPost();

        Comment parentComment;
        if (replyTo.getParentComment() == null) {
            parentComment = replyTo;
        } else {
            parentComment = replyTo.getParentComment();
        }

        Comment comment = commentRepository.save(Comment.builder()
            .content(request.getContent())
            .couple(couple)
            .post(post)
            .parentComment(parentComment)
            .replyToComment(replyTo)
            .build());
        commentStatService.create(comment);

        applicationEventPublisher.publishEvent(
            new CommentReplyCreatedEvent(comment, parentComment, replyTo, post));
    }

    @CheckCommentPermission
    public CommentUpdateResponseDto update(Long commentId,
        CommentUpdateRequestDto request) {
        Comment comment = findById(commentId);
        comment.update(request.getContent());
        return CommentUpdateResponseDto.from(comment);
    }

    @CheckCommentPermission
    public void delete(Long commentId) {
        Comment comment = findById(commentId);

        postStatService.decreaseCommentCount(comment.getPost().getId());
        if (comment.getParentComment() != null) {
            commentStatService.decreaseCommentCount(commentId);
        }

        commentRepository.delete(comment);
    }

    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId)
            .orElseThrow(CommentNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public CursorResponse<ParentCommentResponseDto, CommentCursorResponseDto> getParentComments(
        CommentCursorRequestDto cursor,
        int size, CommentSearchOptions commentSearchOptions, Long postId) {

        List<Comment> parentComments = commentRepository.findParentCommentsByPost(cursor,
            size + 1, commentSearchOptions, postId);

        boolean hasNext = parentComments.size() > size;
        if (hasNext) {
            parentComments = parentComments.subList(0, size);
        }

        CommentCursorResponseDto nextCursor = hasNext && !parentComments.isEmpty()
            ? CommentCursorResponseDto.from(parentComments.getLast())
            : null;

        return new CursorResponse<>(parentComments.stream()
            .map(ParentCommentResponseDto::from)
            .toList(),
            nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public CursorResponse<CommentRepliesResponseDto, CommentCursorResponseDto> getCommentReplies(
        CommentCursorRequestDto cursor,
        int size, Long commentId) {

        List<CommentRepliesResponseDto> replies = commentRepository.findCommentRepliesByComment(
            cursor,
            size + 1, commentId);

        boolean hasNext = replies.size() > size;
        if (hasNext) {
            replies = replies.subList(0, size);
        }

        CommentCursorResponseDto nextCursor = hasNext && !replies.isEmpty()
            ? CommentCursorResponseDto.from(replies.getLast())
            : null;

        return new CursorResponse<>(replies.stream()
            .toList(),
            nextCursor, hasNext);
    }
}
