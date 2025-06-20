package com.duri.domain.post.repository;

import com.duri.domain.post.constant.PostStatus;
import com.duri.domain.post.entity.Post;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long>, SearchPostRepository {


    List<Post> findTop11ByCouple_CodeAndStatus(@Param("code") String code,
        @Param("status") PostStatus status);

    @Query("""
            SELECT p
            FROM Post p
            JOIN FETCH p.couple c
            JOIN FETCH c.userLeft ul
            JOIN FETCH c.userRight ur
            WHERE p.id = :id
        """)
    Optional<Post> findById(@Param("id") Long id);
}
