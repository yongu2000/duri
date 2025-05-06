package com.duri.domain.couple.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.couple.dto.CoupleConnectionCodeResponse;
import com.duri.domain.couple.dto.CoupleConnectionSendRequest;
import com.duri.domain.couple.dto.CoupleConnectionStatusRequest;
import com.duri.domain.couple.dto.CoupleConnectionStatusResponse;
import com.duri.domain.couple.service.CoupleConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/couple/connect")
public class CoupleConnectionController {

    private final CoupleConnectionService coupleConnectionService;

    @GetMapping("/code")
    public ResponseEntity<CoupleConnectionCodeResponse> getUserCode(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(coupleConnectionService.getCode(userDetails));
    }

    @GetMapping("/status/send")
    public ResponseEntity<CoupleConnectionStatusResponse> getSentConnectionStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(coupleConnectionService.getSentConnectionStatus(userDetails));
    }

    @GetMapping("/status/receive")
    public ResponseEntity<CoupleConnectionStatusResponse> getReceivedConnectionStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(coupleConnectionService.getReceivedConnectionStatus(userDetails));
    }

    @PostMapping("/")
    public ResponseEntity<CoupleConnectionStatusResponse> linkCouple(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody CoupleConnectionSendRequest request
    ) {
        return ResponseEntity.ok(coupleConnectionService.connect(userDetails, request));
    }

    @PostMapping("/reject")
    public ResponseEntity<CoupleConnectionStatusResponse> linkCouple(
        @Valid @RequestBody CoupleConnectionStatusRequest request
    ) {
//        return ResponseEntity.ok(coupleService.linkCouple(request));
    }

    @PostMapping("/accept")
    public ResponseEntity<CoupleConnectionStatusResponse> linkCouple(
        @Valid @RequestBody CoupleConnectionStatusRequest request
    ) {
//        return ResponseEntity.ok(coupleService.linkCouple(request));
    }

    @PostMapping("/cancel")
    public ResponseEntity<CoupleConnectionStatusResponse> linkCouple(
        @Valid @RequestBody CoupleConnectionStatusRequest request
    ) {
//        return ResponseEntity.ok(coupleService.linkCouple(request));
    }
}

