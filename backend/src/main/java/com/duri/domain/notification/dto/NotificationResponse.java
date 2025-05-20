package com.duri.domain.notification.dto;

import com.duri.domain.notification.constant.NotificationType;
import com.duri.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationResponse {

    private String content;
    private String fromUser;
    private NotificationType type;
    private boolean confirmed;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getContent(), notification.getFrom().getName(),
            notification.getType(), notification.isConfirmed(), notification.getCreatedAt());
    }
}
