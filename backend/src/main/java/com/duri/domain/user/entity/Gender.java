package com.duri.domain.user.entity;

import com.duri.domain.user.exception.InvalidGenderException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE,
    FEMALE;

    public static Gender of(String gender) {
        return switch (gender) {
            case "male", "boy" -> MALE;
            case "female", "girl" -> FEMALE;
            default -> throw new InvalidGenderException();
        };
    }

    @Override
    public String toString() {
        return this.name();
    }
}
