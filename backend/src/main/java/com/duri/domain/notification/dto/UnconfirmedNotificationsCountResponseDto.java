package com.duri.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnconfirmedNotificationsCountResponseDto {

    private long count;

    public static UnconfirmedNotificationsCountResponseDto of(long count) {
        return new UnconfirmedNotificationsCountResponseDto(count);
    }
}
