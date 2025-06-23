package com.duri.domain.post.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.post.dto.comment.CommentCreateRequestDto;
import com.duri.domain.post.dto.comment.CommentCursorRequestDto;
import com.duri.domain.post.dto.comment.CommentCursorResponseDto;
import com.duri.domain.post.dto.comment.CommentRepliesResponseDto;
import com.duri.domain.post.dto.comment.CommentReplyCreateRequestDto;
import com.duri.domain.post.dto.comment.CommentSearchOptions;
import com.duri.domain.post.dto.comment.CommentUpdateRequestDto;
import com.duri.domain.post.dto.comment.CommentUpdateResponseDto;
import com.duri.domain.post.dto.comment.ParentCommentResponseDto;
import com.duri.domain.post.service.CommentService;
import com.duri.global.annotation.DecryptId;
import com.duri.global.dto.CursorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}")
    public ResponseEntity<Void> create(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody CommentCreateRequestDto request,
        @PathVariable @DecryptId Long postId) {
        commentService.create(userDetails.getUser().getCoupleCode(), postId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reply/{commentId}")
    public ResponseEntity<Void> createReply(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody CommentReplyCreateRequestDto request,
        @PathVariable @DecryptId Long commentId) {
        commentService.createReply(userDetails.getUser().getCoupleCode(), commentId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentUpdateResponseDto> update(
        @RequestBody CommentUpdateRequestDto request,
        @PathVariable @DecryptId Long commentId) {
        return ResponseEntity.ok(commentService.update(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
        @PathVariable @DecryptId Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<CursorResponse<ParentCommentResponseDto, CommentCursorResponseDto>> getPostParentComments(
        @ModelAttribute CommentCursorRequestDto cursor,
        @RequestParam(defaultValue = "10") int size,
        @ModelAttribute CommentSearchOptions commentSearchOptions,
        @PathVariable @DecryptId Long postId
    ) {
        return ResponseEntity.ok(
            commentService.getParentComments(cursor, size, commentSearchOptions, postId));
    }

    @GetMapping("/reply/{commentId}")
    public ResponseEntity<CursorResponse<CommentRepliesResponseDto, CommentCursorResponseDto>> getCommentReplies(
        @ModelAttribute CommentCursorRequestDto cursor,
        @RequestParam(defaultValue = "10") int size,
        @PathVariable @DecryptId Long commentId) {
        return ResponseEntity.ok(commentService.getCommentReplies(cursor, size, commentId));
    }

}
