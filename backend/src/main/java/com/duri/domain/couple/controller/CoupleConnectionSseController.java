package com.duri.domain.couple.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.couple.service.CoupleConnectionSseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class CoupleConnectionSseController {

    private final CoupleConnectionSseEmitterService coupleConnectionSseEmitterService;

    @GetMapping("/sse/couple/status")
    public SseEmitter connect(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        return coupleConnectionSseEmitterService.subscribe(username);
    }
}
