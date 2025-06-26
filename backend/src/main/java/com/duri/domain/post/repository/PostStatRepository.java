package com.duri.domain.post.repository;

import com.duri.domain.post.entity.PostStat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostStatRepository extends JpaRepository<PostStat, Long> {


    Optional<PostStat> findByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStat ps SET ps.likeCount = ps.likeCount + 1 WHERE ps.post.id = :postId")
    void increaseLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStat ps SET ps.likeCount = ps.likeCount - 1 WHERE ps.post.id = :postId")
    void decreaseLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStat ps SET ps.commentCount = ps.commentCount + 1 WHERE ps.post.id = :postId")
    void increaseCommentCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStat ps SET ps.commentCount = ps.commentCount - 1 WHERE ps.post.id = :postId")
    void decreaseCommentCount(@Param("postId") Long postId);
}
