package com.duri.domain.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final StringRedisTemplate redisTemplate;

    public void publishCoupleConnection(String username, String eventName, String data) {
        String message = username + "|" + eventName + "|" + data;
        redisTemplate.convertAndSend("couple-connect-channel", message);
    }

    public void publishNotification(String username, String eventName, String data) {
        String message = username + "|" + eventName + "|" + data;
        redisTemplate.convertAndSend("notification-channel", message);
    }
}
