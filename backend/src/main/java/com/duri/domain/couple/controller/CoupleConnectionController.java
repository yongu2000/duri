package com.duri.domain.couple.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.couple.dto.coupleconnection.CoupleConnectionCodeResponse;
import com.duri.domain.couple.dto.coupleconnection.CoupleConnectionSendRequest;
import com.duri.domain.couple.dto.coupleconnection.CoupleConnectionStatusResponse;
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
        return ResponseEntity.ok(
            coupleConnectionService.getSentConnectionStatus(userDetails));
    }

    @GetMapping("/status/receive")
    public ResponseEntity<CoupleConnectionStatusResponse> getReceivedConnectionStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
            coupleConnectionService.getReceivedConnectionStatus(userDetails));
    }

    @PostMapping("/status/confirm")
    public ResponseEntity<Void> confirmConnectionStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
            coupleConnectionService.confirmConnectionStatus(userDetails));
    }

    @PostMapping
    public ResponseEntity<CoupleConnectionStatusResponse> linkCouple(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody CoupleConnectionSendRequest request
    ) {
        return ResponseEntity.ok(coupleConnectionService.connect(userDetails, request));
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> rejectConnection(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(coupleConnectionService.rejectConnection(userDetails));
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> acceptConnection(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(coupleConnectionService.acceptConnection(userDetails));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelConnection(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(coupleConnectionService.cancelConnection(userDetails));
    }
}

