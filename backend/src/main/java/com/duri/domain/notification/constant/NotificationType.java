package com.duri.domain.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    POST, LIKE, COMMENT;

    @Override
    public String toString() {
        return this.name();
    }
}
