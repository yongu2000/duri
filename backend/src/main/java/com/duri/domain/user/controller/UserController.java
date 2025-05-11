package com.duri.domain.user.controller;

import com.duri.domain.user.dto.PasswordResetRequest;
import com.duri.domain.user.dto.UserProfileEditRequest;
import com.duri.domain.user.dto.UserResponse;
import com.duri.domain.user.service.UserService;
import com.duri.global.dto.DuplicateCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserResponse> getUserProfile() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserProfile());
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
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.editUserProfile(username, request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetUserPassword(@RequestBody PasswordResetRequest request) {
        userService.resetUserPassword(request);
        return ResponseEntity.ok().build();
    }
}
