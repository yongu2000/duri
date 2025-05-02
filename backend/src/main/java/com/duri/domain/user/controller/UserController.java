package com.duri.domain.user.controller;

import com.duri.domain.user.dto.UserResponse;
import com.duri.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
