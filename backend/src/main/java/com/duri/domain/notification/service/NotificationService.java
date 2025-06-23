package com.duri.domain.notification.service;

import com.duri.domain.notification.annotation.CheckNotificationPermission;
import com.duri.domain.notification.dto.NotificationCursorRequest;
import com.duri.domain.notification.dto.NotificationCursorResponse;
import com.duri.domain.notification.dto.NotificationResponse;
import com.duri.domain.notification.dto.UnconfirmedNotificationsCountResponseDto;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.notification.exception.NotificationNotFoundException;
import com.duri.domain.notification.repository.NotificationRepository;
import com.duri.domain.sse.RedisMessagePublisher;
import com.duri.global.dto.CursorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RedisMessagePublisher publisher;

    public void send(Notification notification) {
        notificationRepository.save(notification);
        publisher.publishNotification(notification.getTo().getUsername(),
            notification.getType().toString(),
            notification.getContent());
    }

    @Transactional(readOnly = true)
    public UnconfirmedNotificationsCountResponseDto getUnconfirmedNotificationsCount(Long userId) {
        List<Notification> notifications = notificationRepository.findTop100ByTo_IdAndConfirmedFalse(
            (userId));

        return UnconfirmedNotificationsCountResponseDto.of(notifications.size());
    }

    @Transactional
    public CursorResponse<NotificationResponse, NotificationCursorResponse> getUnconfirmedNotifications(
        NotificationCursorRequest cursor, int size, Long userId) {
        // 커서 기반 조회
        List<Notification> notifications = notificationRepository.findUnconfirmedNotifications(
            cursor, size + 1, userId);

        // 읽음으로 변경
//        confirmNotification(notifications);

        // hasNext 확인을 위해 size + 1개를 조회했으므로, 실제 응답에는 size개만 포함
        boolean hasNext = notifications.size() > size;
        if (hasNext) {
            notifications = notifications.subList(0, size);
        }

        NotificationCursorResponse nextCursor = hasNext && !notifications.isEmpty()
            ? NotificationCursorResponse.from(notifications.getLast())
            : null;

        // 다음 커서는 마지막 게시글의 ID
        return new CursorResponse<>(notifications.stream().map(NotificationResponse::from).toList(),
            nextCursor, hasNext);

    }

    private void confirmNotification(List<Notification> notifications) {
        List<Long> ids = notifications.stream()
            .map(Notification::getId)
            .toList();
        if (!ids.isEmpty()) {
            notificationRepository.updateConfirmedByIds(ids);
        }
    }

    @Transactional(readOnly = true)
    public CursorResponse<NotificationResponse, NotificationCursorResponse> getConfirmedNotifications(
        NotificationCursorRequest cursor, int size, Long userId) {
        // 커서 기반 조회
        List<Notification> notifications = notificationRepository.findConfirmedNotifications(
            cursor, size + 1, userId);

        // hasNext 확인을 위해 size + 1개를 조회했으므로, 실제 응답에는 size개만 포함
        boolean hasNext = notifications.size() > size;
        if (hasNext) {
            notifications = notifications.subList(0, size);
        }

        NotificationCursorResponse nextCursor = hasNext && !notifications.isEmpty()
            ? NotificationCursorResponse.from(notifications.getLast())
            : null;

        // 다음 커서는 마지막 게시글의 ID
        return new CursorResponse<>(notifications.stream().map(NotificationResponse::from).toList(),
            nextCursor, hasNext);

    }

    @CheckNotificationPermission
    public void delete(Long notificationId) {
        Notification notification = findById(notificationId);
        notificationRepository.delete(notification);
    }

    public void deleteAll(Long userId) {
        notificationRepository.deleteByToId(userId);
    }

    public Notification findById(Long notificationId) {
        return notificationRepository.findById(notificationId)
            .orElseThrow(NotificationNotFoundException::new);
    }
}

