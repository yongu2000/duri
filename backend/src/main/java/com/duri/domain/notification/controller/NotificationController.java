package com.duri.domain.notification.controller;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.notification.dto.NotificationCursor;
import com.duri.domain.notification.dto.NotificationResponse;
import com.duri.domain.notification.dto.UnconfirmedNotificationsCountResponseDto;
import com.duri.domain.notification.service.NotificationService;
import com.duri.domain.sse.service.SSEService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
@Slf4j
public class NotificationController {

    private final SSEService sseService;
    private final NotificationService notificationService;

    @GetMapping("/sse")
    public SseEmitter connect(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        return sseService.subscribe(username);
    }

    @GetMapping("/unconfirmed")
    public ResponseEntity<CursorResponse<NotificationResponse, NotificationCursor>> getUnconfirmedNotifications(
        @ModelAttribute NotificationCursor cursor,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
            notificationService.getUnconfirmedNotifications(cursor, size, userDetails.getId()));
    }

    @GetMapping("/confirmed")
    public ResponseEntity<CursorResponse<NotificationResponse, NotificationCursor>> getConfirmedNotifications(
        @ModelAttribute NotificationCursor cursor,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
            notificationService.getConfirmedNotifications(cursor, size, userDetails.getId()));
    }

    @GetMapping("/unconfirmed/count")
    public ResponseEntity<UnconfirmedNotificationsCountResponseDto> getUnconfirmedNotificationsCount(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
            notificationService.getUnconfirmedNotificationsCount(userDetails.getId()));
    }

    @DeleteMapping("/delete/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
        @PathVariable @DecryptId Long notificationId
    ) {
        notificationService.delete(notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/all")
    public ResponseEntity<Void> deleteAllNotification(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.deleteAll(userDetails.getId());
        return ResponseEntity.ok().build();
    }

}
