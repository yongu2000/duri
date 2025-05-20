package com.duri.domain.notification.service;

import com.duri.domain.notification.dto.MainNotificationResponse;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.notification.repository.NotificationRepository;
import com.duri.domain.sse.service.SSEService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SSEService sseService;

    public void send(Notification notification) {
        notificationRepository.save(notification);
        sseService.send(notification.getTo().getUsername(), notification.getType().toString(),
            notification.getContent());
    }

    public MainNotificationResponse getMainNotification(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        return MainNotificationResponse.from(notifications);
    }
}
