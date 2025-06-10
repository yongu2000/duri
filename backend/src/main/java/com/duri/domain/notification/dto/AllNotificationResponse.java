package com.duri.domain.notification.dto;

import com.duri.domain.notification.entity.Notification;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AllNotificationResponse {

    private List<NotificationResponse> notifications;

    public static AllNotificationResponse from(List<Notification> notifications) {
        return new AllNotificationResponse(
            notifications.stream().map(NotificationResponse::from).toList());
    }
}
