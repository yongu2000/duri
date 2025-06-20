package com.duri.domain.user.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.user.dto.PasswordResetRequest;
import com.duri.domain.user.dto.UserProfileEditRequest;
import com.duri.domain.user.dto.UserResponse;
import com.duri.domain.user.service.UserService;
import com.duri.global.dto.DuplicateCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/my")
    public ResponseEntity<UserResponse> getUserProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(UserResponse.from(userDetails.getUser()));
    }

    @GetMapping("/check/email/{email}")
    public ResponseEntity<DuplicateCheckResponse> checkEmailDuplicate(
        @PathVariable String email) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.checkEmailDuplicate(email));
    }

    @GetMapping("/check/username/{username}")
    public ResponseEntity<DuplicateCheckResponse> checkUsernameDuplicate(
        @PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.checkUsernameDuplicate(username));
    }

    @PutMapping("/profile/{username}/edit")
    public ResponseEntity<Void> editUserProfile(@PathVariable String username,
        @RequestBody UserProfileEditRequest request) {
        userService.editUserProfile(username, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetUserPassword(@RequestBody PasswordResetRequest request) {
        userService.resetUserPassword(request);
        return ResponseEntity.ok().build();
    }
}
