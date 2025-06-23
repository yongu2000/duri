package com.duri.domain.notification.dto;

import com.duri.domain.notification.entity.Notification;
import com.duri.global.util.AESUtil;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class NotificationCursorResponse {

    private LocalDateTime createdAt;
    private String id;

    public static NotificationCursorResponse from(Notification notification) {
        try {
            return new NotificationCursorResponse(
                notification.getCreatedAt(),
                AESUtil.encrypt(String.valueOf(notification.getId()))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

