package com.duri.domain.couple.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoupleConnectionStatus {
    PENDING,
    ACCEPT,
    REJECT,
    CANCEL;

    @Override
    public String toString() {
        return this.name();
    }

}
