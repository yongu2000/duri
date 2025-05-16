package com.duri.domain.user.dto;

import com.duri.domain.user.entity.Gender;
import com.duri.domain.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserResponse {

    private String email;
    private String username;
    private String name;
    private String coupleCode;
    private String profileImageUrl;
    private LocalDate birthday;
    private Gender gender;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
            .email(user.getEmail())
            .username(user.getUsername())
            .name(user.getName())
            .coupleCode(user.getCoupleCode())
            .profileImageUrl(user.getProfileImageUrl())
            .birthday(user.getBirthday())
            .gender(user.getGender())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
