package com.duri.domain.post.service;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.service.CoupleService;
import com.duri.domain.post.annotation.CheckCommentPermission;
import com.duri.domain.post.dto.comment.CommentCreateRequestDto;
import com.duri.domain.post.dto.comment.CommentRepliesResponseDto;
import com.duri.domain.post.dto.comment.CommentReplyCreateRequestDto;
import com.duri.domain.post.dto.comment.CommentUpdateRequestDto;
import com.duri.domain.post.dto.comment.CommentUpdateResponseDto;
import com.duri.domain.post.dto.comment.ParentCommentResponseDto;
import com.duri.domain.post.entity.Comment;
import com.duri.domain.post.entity.CommentStat;
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.exception.CommentNotFoundException;
import com.duri.domain.post.repository.CommentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentStatService commentStatService;
    private final CoupleService coupleService;
    private final PostService postService;
    private final PostStatService postStatService;

    public void create(String coupleCode, Long postId, CommentCreateRequestDto request) {
        Couple couple = coupleService.findByCode(coupleCode);
        Post post = postService.findById(postId);
        String content = request.getContent();

        postStatService.increaseCommentCount(post.getId());

        Comment comment = commentRepository.save(Comment.builder()
            .content(content)
            .couple(couple)
            .post(post)
            .build());

        commentStatService.create(comment);
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
        
        commentStatService.increaseCommentCount(parentComment.getId());
        postStatService.increaseCommentCount(post.getId());

        commentRepository.save(Comment.builder()
            .content(request.getContent())
            .couple(couple)
            .post(post)
            .parentComment(parentComment)
            .replyToComment(replyTo)
            .build());
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
    public List<ParentCommentResponseDto> getPostParentComments(Long postId) {
        return commentRepository.findParentCommentsByPostId(postId).stream().map((comment) -> {
                CommentStat commentStat = commentStatService.findByCommentId(comment.getId());
                return ParentCommentResponseDto.from(comment, commentStat);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentRepliesResponseDto> getCommentReplies(Long commentId) {
        return commentRepository.findByParentCommentId(commentId).stream()
            .map(CommentRepliesResponseDto::from)
            .toList();
    }
}
