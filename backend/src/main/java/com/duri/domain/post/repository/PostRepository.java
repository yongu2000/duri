package com.duri.domain.post.repository;

import com.duri.domain.post.constant.PostStatus;
import com.duri.domain.post.entity.Post;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long>, SearchPostRepository {

    @Query("""
            SELECT COUNT(p)
            FROM Post p
            JOIN p.couple c
            WHERE c.code = :code
            AND p.status = :status
        """)
    long countByCodeAndStatus(@Param("code") String code, @Param("status") PostStatus status);
}
