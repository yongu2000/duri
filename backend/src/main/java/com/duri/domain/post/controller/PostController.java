package com.duri.domain.post.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.post.dto.PendingPostCountResponse;
import com.duri.domain.post.dto.PostCreateRequest;
import com.duri.domain.post.dto.PostCursor;
import com.duri.domain.post.dto.PostEditRequest;
import com.duri.domain.post.dto.PostIdToken;
import com.duri.domain.post.dto.PostImageUrlResponse;
import com.duri.domain.post.dto.PostResponse;
import com.duri.domain.post.dto.PostSearchOptions;
import com.duri.domain.post.service.PostService;
import com.duri.global.dto.CursorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<Void> createPost(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody PostCreateRequest request
    ) {
        postService.create(userDetails, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/complete")
    public ResponseEntity<CursorResponse<PostResponse, PostCursor>> getCompletePosts(
        @ModelAttribute PostCursor cursor,
        @RequestParam(defaultValue = "10") int size,
        @ModelAttribute PostSearchOptions postSearchOptions

    ) {
        return ResponseEntity.ok(
            postService.getAllPostsWithSearchOptionsToCursor(cursor, size, postSearchOptions));
    }

    @GetMapping("/image")
    public ResponseEntity<List<PostImageUrlResponse>> getPostImages(
        PostIdToken postIdToken
    ) {
        return ResponseEntity.ok(postService.getPostImages(postIdToken));
    }

    @GetMapping("/complete/{coupleCode}")
    public ResponseEntity<CursorResponse<PostResponse, PostCursor>> getCompletePostsByCouple(
        @ModelAttribute PostCursor cursor,
        @RequestParam(defaultValue = "10") int size,
        @ModelAttribute PostSearchOptions postSearchOptions,
        @PathVariable String coupleCode
    ) {
        return ResponseEntity.ok(
            postService.getAllPostsWithSearchOptionsToCursor(cursor, size, postSearchOptions,
                coupleCode));
    }

    @GetMapping("/pending/{coupleCode}/count")
    public ResponseEntity<PendingPostCountResponse> getPendingPostsCount(
        @PathVariable String coupleCode
    ) {
        return ResponseEntity.ok(
            postService.getPendingPostCount(coupleCode));
    }

    @GetMapping("/pending/{coupleCode}")
    public ResponseEntity<CursorResponse<PostResponse, PostCursor>> getPendingPostsByCouple(
        @ModelAttribute PostCursor cursor,
        @RequestParam(defaultValue = "10") int size,
        @ModelAttribute PostSearchOptions postSearchOptions,
        @PathVariable String coupleCode
    ) {
        return ResponseEntity.ok(
            postService.getPendingPostsWithSearchOptionsToCursor(cursor, size, postSearchOptions,
                coupleCode));
    }

    @GetMapping("/edit")
    public ResponseEntity<PostResponse> getPost(
        PostIdToken postIdToken
    ) {
        return ResponseEntity.ok(postService.getPost(postIdToken));
    }

    @PutMapping("/edit")
    public ResponseEntity<Void> editPost(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody PostEditRequest request
    ) {
        postService.edit(userDetails, request);
        return ResponseEntity.ok().build();
    }

}
