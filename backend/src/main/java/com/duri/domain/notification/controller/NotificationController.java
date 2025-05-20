package com.duri.domain.notification.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.notification.dto.MainNotificationResponse;
import com.duri.domain.notification.service.NotificationService;
import com.duri.domain.sse.service.SSEService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final SSEService sseService;
    private final NotificationService notificationService;

    @GetMapping("/sse")
    public SseEmitter connect(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        return sseService.subscribe(username);
    }

    @GetMapping("/main")
    public ResponseEntity<MainNotificationResponse> postNotification(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
            notificationService.getMainNotification(userDetails.getId()));
    }
    
}
