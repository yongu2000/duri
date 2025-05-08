package com.duri.domain.couple.controller;

import com.duri.domain.couple.dto.CoupleConnectionStatusResponse;
import com.duri.domain.couple.entity.CoupleConnection;
import com.duri.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class CoupleConnectionWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendConnectionRequest(CoupleConnection response) {
        messagingTemplate.convertAndSend("/topic/couple/" + response.getRespondent().getId(),
            CoupleConnectionStatusResponse.of(response));
    }

    public void sendConnectionStatus(User targetUser, CoupleConnection response) {
        messagingTemplate.convertAndSend("/topic/couple/status" + targetUser.getId(),
            CoupleConnectionStatusResponse.of(response));
    }
}

