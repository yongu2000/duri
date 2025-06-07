package com.duri.domain.post.repository;

import com.duri.domain.post.entity.CommentStat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentStatRepository extends JpaRepository<CommentStat, Long> {

    Optional<CommentStat> findByCommentId(Long commentId);

}
