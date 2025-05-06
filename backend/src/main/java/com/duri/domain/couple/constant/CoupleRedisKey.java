package com.duri.domain.couple.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoupleRedisKey {
    COUPLE_CONNECTION_USERID_TO_CODE_KEY("COUPLE:CONNECTION:USERID:"),
    COUPLE_CONNECTION_CODE_TO_USERID_KEY("COUPLE:CONNECTION:CODE:");

    private final String value;

    @Override
    public String toString() {
        return value;
    }

}
