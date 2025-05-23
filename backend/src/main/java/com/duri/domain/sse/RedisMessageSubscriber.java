package com.duri.domain.sse;

import com.duri.domain.couple.service.CoupleConnectionSseEmitterService;
import com.duri.domain.sse.service.SSEService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SSEService sseService;
    private final CoupleConnectionSseEmitterService coupleConnectionSSEService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String rawMessage = message.toString(); // 메시지: username|eventName|data
            String[] parts = rawMessage.split("\\|", 3);
            if (parts.length == 3) {
                String username = parts[0];
                String eventName = parts[1];
                String data = parts[2];

                log.info("Redis에서 수신한 메시지: user={}, event={}, data={}", username, eventName, data);
                coupleConnectionSSEService.send(username, eventName, data);
                sseService.send(username, eventName, data);
            } else {
                log.warn("잘못된 메시지 형식: {}", rawMessage);
            }
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패", e);
        }
    }
}
