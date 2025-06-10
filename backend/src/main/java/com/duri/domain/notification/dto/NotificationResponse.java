package com.duri.domain.notification.dto;

import com.duri.domain.notification.constant.NotificationType;
import com.duri.domain.notification.entity.Notification;
import com.duri.global.util.AESUtil;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String content;
    private String fromUser;
    private NotificationType type;
    private boolean confirmed;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            AESUtil.encrypt(String.valueOf(notification.getId())),
            notification.getContent(),
            notification.getFrom() != null ? notification.getFrom().getName()
                : notification.getFromCouple().getName(),
            notification.getType(),
            notification.isConfirmed(),
            notification.getCreatedAt());
    }
}
