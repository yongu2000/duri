package com.duri.domain.notification.repository;

import com.duri.domain.notification.dto.NotificationCursor;
import com.duri.domain.notification.entity.Notification;
import java.util.List;

public interface NotificationSearchRepository {

    List<Notification> findUnconfirmedNotifications(NotificationCursor cursor, int size,
        Long userId);

    List<Notification> findConfirmedNotifications(NotificationCursor cursor, int size,
        Long userId);

}
