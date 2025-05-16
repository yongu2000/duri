package com.duri.domain.image.repository;

import com.duri.domain.image.entity.Image;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

    void deleteByUrl(String url);

    Optional<Image> findByUrl(String url);

    List<Image> findByPostId(Long postId);
}
