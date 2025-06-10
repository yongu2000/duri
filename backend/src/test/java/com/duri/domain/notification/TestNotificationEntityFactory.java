package com.duri.domain.notification;

import com.duri.domain.notification.constant.NotificationType;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.user.entity.User;
import com.duri.global.entity.BaseEntity;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class TestNotificationEntityFactory {

    public static Notification createNotification(User to, User from, String content,
        NotificationType type,
        boolean confirmed) {
        return Notification.builder()
            .to(to)
            .from(from)
            .type(type)
            .content(content)
            .confirmed(confirmed)
            .build();
    }

    public static Notification createNotificationWithCreatedAt(User to, User from, String content,
        NotificationType type,
        boolean confirmed, LocalDateTime createdAt) {
        Notification notification = createNotification(to, from, content, type, confirmed);
        try {
            Field field = BaseEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(notification, createdAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return notification;
    }
}
