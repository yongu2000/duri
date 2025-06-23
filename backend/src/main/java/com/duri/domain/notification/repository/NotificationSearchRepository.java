package com.duri.domain.notification.repository;

import com.duri.domain.notification.dto.NotificationCursorRequest;
import com.duri.domain.notification.entity.Notification;
import java.util.List;

public interface NotificationSearchRepository {

    List<Notification> findUnconfirmedNotifications(NotificationCursorRequest cursor, int size,
        Long userId);

    List<Notification> findConfirmedNotifications(NotificationCursorRequest cursor, int size,
        Long userId);

}
