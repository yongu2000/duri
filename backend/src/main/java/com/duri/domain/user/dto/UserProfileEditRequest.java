package com.duri.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileEditRequest {

    private String profileImageUrl;
    private String name;
}
