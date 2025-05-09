package com.duri.domain.couple.controller;

import com.duri.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class CoupleConnectionWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendConnectionStatusRetrieveMessage(User targetUser) {
        messagingTemplate.convertAndSend(
            "/topic/couple/status" + targetUser.getUsername(),
            true);
    }
}

