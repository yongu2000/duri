package com.duri.domain.post.repository;

import com.duri.domain.post.entity.PostStat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostStatRepository extends JpaRepository<PostStat, Long> {

    Optional<PostStat> findByPostId(Long postId);
}
