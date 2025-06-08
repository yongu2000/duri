package com.duri.domain.notification.dto;

import com.duri.domain.notification.entity.Notification;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MainNotificationResponse {

    private Long count;
    private List<NotificationResponse> notifications;

    public static MainNotificationResponse from(List<Notification> notifications) {
        return new MainNotificationResponse((long) notifications.size(),
            notifications.stream().map(NotificationResponse::from).toList());
    }
}
