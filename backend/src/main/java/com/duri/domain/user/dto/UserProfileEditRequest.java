package com.duri.domain.user.dto;

import com.duri.domain.user.entity.Gender;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileEditRequest {

    private String profileImageUrl;
    private String name;
    private Gender gender;
    private LocalDate birthday;
}
