package com.duri.domain.post.service;

import com.duri.domain.post.entity.Comment;
import com.duri.domain.post.entity.CommentStat;
import com.duri.domain.post.exception.CommentStatNotFoundException;
import com.duri.domain.post.repository.comment.CommentStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentStatService {

    private final CommentStatRepository commentStatRepository;

    public CommentStat findByCommentId(Long commentId) {
        return commentStatRepository.findByCommentId(commentId)
            .orElseThrow(CommentStatNotFoundException::new);
    }

    public void create(Comment comment) {
        commentStatRepository.save(CommentStat.builder()
            .comment(comment)
            .build());
    }

    public void increaseCommentCount(Long commentId) {
        CommentStat commentStat = findByCommentId(commentId);

        commentStat.increaseCommentCount();
    }

    public void decreaseCommentCount(Long commentId) {
        CommentStat postStat = findByCommentId(commentId);

        postStat.decreaseCommentCount();
    }
}

