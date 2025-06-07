package com.duri.domain.post.repository;

import com.duri.domain.post.entity.Comment;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.post.id = :postId AND c.parentComment IS NULL
        """)
    List<Comment> findParentCommentsByPostId(@Param("postId") Long postId);

    List<Comment> findByParentCommentId(Long parentCommentId);
}
