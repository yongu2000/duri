package com.duri.domain.notification.dto;

import com.duri.domain.notification.entity.Notification;
import com.duri.global.annotation.DecryptId;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class NotificationCursor {

    private LocalDateTime date;
    @DecryptId
    private Long id;

    public static NotificationCursor from(Notification notification) {
        try {
            return new NotificationCursor(
                notification.getCreatedAt(),
                notification.getId()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

