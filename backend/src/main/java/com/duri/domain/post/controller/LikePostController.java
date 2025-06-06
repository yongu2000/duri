package com.duri.domain.post.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.post.dto.PostIdToken;
import com.duri.domain.post.dto.PostLikeStatusResponseDto;
import com.duri.domain.post.service.LikePostService;
import com.duri.global.util.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class LikePostController {

    private final LikePostService likePostService;

    @PostMapping("/like")
    public ResponseEntity<Void> likePost(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody PostIdToken postIdToken) {
        Long postId = Long.parseLong(AESUtil.decrypt(postIdToken.getPostIdToken()));
        likePostService.like(userDetails.getUser().getCoupleCode(), postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dislike")
    public ResponseEntity<Void> dislikePost(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody PostIdToken postIdToken) {
        Long postId = Long.parseLong(AESUtil.decrypt(postIdToken.getPostIdToken()));
        likePostService.dislike(userDetails.getUser().getCoupleCode(), postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/like/status")
    public ResponseEntity<PostLikeStatusResponseDto> getLikeStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam PostIdToken postIdToken) {
        Long postId = Long.parseLong(AESUtil.decrypt(postIdToken.getPostIdToken()));
        return ResponseEntity.ok(
            likePostService.getLikeStatus(userDetails.getUser().getCoupleCode(), postId));
    }
}
