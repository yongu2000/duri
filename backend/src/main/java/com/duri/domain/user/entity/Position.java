package com.duri.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Position {
    LEFT,
    RIGHT;

    @Override
    public String toString() {
        return this.name();
    }
}
