package com.duri.domain.auth.jwt.controller;

import com.duri.domain.auth.jwt.dto.JoinRequest;
import com.duri.domain.auth.jwt.dto.JoinResponse;
import com.duri.domain.auth.jwt.service.JoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/join")
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping
    public ResponseEntity<JoinResponse> join(@Valid @RequestBody JoinRequest request) {
        JoinResponse response = joinService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
