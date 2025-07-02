package com.duri.domain.post.repository.comment;

import com.duri.domain.post.dto.comment.CommentCursorRequestDto;
import com.duri.domain.post.dto.comment.CommentRepliesResponseDto;
import com.duri.domain.post.dto.comment.CommentSearchOptions;
import com.duri.domain.post.entity.Comment;
import java.util.List;

public interface SearchCommentRepository {

    List<Comment> findParentCommentsByPost(CommentCursorRequestDto cursor, int size,
        CommentSearchOptions searchOptions, Long postId);

    List<CommentRepliesResponseDto> findCommentRepliesByComment(CommentCursorRequestDto cursor,
        int size,
        Long commentId);
}
