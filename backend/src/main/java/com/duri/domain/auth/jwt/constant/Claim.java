package com.duri.domain.auth.jwt.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Claim {
    HEADER_JWT("JWT"),
    TOKEN_TYPE("token_type"),
    ID("id");

    private final String value;
}
