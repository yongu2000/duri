package com.duri.domain.email.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailRedisKey {
    EMAIL_VERIFICATION_KEY("EMAIL:VERIFICATION:"),
    EMAIL_VERIFIED_KEY("EMAIL:VERIFIED:"),
    PASSWORD_RESET_TOKEN_KEY("PASSWORD:RESET:TOKEN:");

    private final String value;

    @Override
    public String toString() {
        return value;
    }

}
