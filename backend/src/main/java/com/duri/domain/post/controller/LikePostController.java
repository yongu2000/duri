package com.duri.domain.post.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.post.dto.PostLikeStatusResponseDto;
import com.duri.domain.post.service.LikePostService;
import com.duri.global.annotation.DecryptId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class LikePostController {

    private final LikePostService likePostService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable @DecryptId Long postId) {
        likePostService.like(userDetails.getUser().getCoupleCode(), postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/dislike")
    public ResponseEntity<Void> dislikePost(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable @DecryptId Long postId) {
        likePostService.dislike(userDetails.getUser().getCoupleCode(), postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}/like/status")
    public ResponseEntity<PostLikeStatusResponseDto> getLikeStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable @DecryptId Long postId) {
        return ResponseEntity.ok(
            likePostService.getLikeStatus(userDetails.getUser().getCoupleCode(), postId));
    }
}
