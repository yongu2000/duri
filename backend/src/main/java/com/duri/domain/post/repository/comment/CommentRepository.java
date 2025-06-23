package com.duri.domain.post.repository.comment;

import com.duri.domain.post.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, SearchCommentRepository {

}
