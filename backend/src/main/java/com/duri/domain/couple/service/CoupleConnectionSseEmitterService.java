package com.duri.domain.couple.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class CoupleConnectionSseEmitterService {

    private static final Long TIMEOUT = 60L * 1000 * 60; // 1시간
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String username) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(username, emitter);

        emitter.onCompletion(() -> emitters.remove(username));
        emitter.onTimeout(() -> emitters.remove(username));

        return emitter;
    }

    public void send(String username, Object data) {
        SseEmitter emitter = emitters.get(username);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("couple-status").data(data));
            } catch (IOException e) {
                emitters.remove(username);
            }
        }
    }
}
