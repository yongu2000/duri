package com.duri.domain.sse.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
public class SSEService {

    private static final Long TIMEOUT = 60L * 1000 * 60; // 1시간
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();


    public SseEmitter subscribe(String username) {
        // 이전 연결이 있다면 제거
        SseEmitter oldEmitter = emitters.get(username);
        if (oldEmitter != null) {
            oldEmitter.complete();
            emitters.remove(username);
        }

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(username, emitter);

        // 연결 완료 시 처리
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료: {}", username);
            emitters.remove(username);
        });

        // 타임아웃 시 처리
        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: {}", username);
            emitters.remove(username);
        });

        // 에러 발생 시 처리
        emitter.onError((ex) -> {
            log.error("SSE 연결 에러: {}", username, ex);
            emitters.remove(username);
        });

        // 초기 연결 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("Connected"));
        } catch (IOException e) {
            log.error("초기 연결 메시지 전송 실패: {}", username, e);
        }

        return emitter;
    }

    public void send(String username, String eventName, Object data) {
        SseEmitter emitter = emitters.get(username);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            } catch (IOException e) {
                log.error("SSE 메시지 전송 실패: {}", username, e);
                emitters.remove(username);
                emitter.complete();
            }
        }
    }

    // 연결 상태 확인 메서드 추가
    public boolean isConnected(String username) {
        SseEmitter emitter = emitters.get(username);
        return emitter != null;
    }

    // 주기적인 하트비트 전송
    @Scheduled(fixedRate = 30000) // 30초마다
    public void sendHeartbeat() {
        emitters.forEach((username, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data("heartbeat"));
            } catch (IOException e) {
                log.error("하트비트 전송 실패: {}", username, e);
                emitters.remove(username);
                emitter.complete();
            }
        });
    }
}
