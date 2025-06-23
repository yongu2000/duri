package com.duri.domain.notification.dto;

import com.duri.global.annotation.DecryptId;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class NotificationCursorRequest {

    private LocalDateTime createdAt;
    @DecryptId
    private Long id;
}

