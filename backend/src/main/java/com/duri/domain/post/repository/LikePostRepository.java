package com.duri.domain.post.repository;

import com.duri.domain.post.entity.LikePost;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikePostRepository extends JpaRepository<LikePost, Long> {

    Optional<LikePost> findByCoupleIdAndPostId(Long coupleId, Long postId);
}
