package com.duri.domain.couple.dto.coupleprofile;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.user.entity.Gender;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CoupleProfileResponse {

    private String coupleName;
    private String coupleCode;
    private String userLeftName;
    private Gender userLeftGender;
    private LocalDate userLeftBirthday;
    private String userLeftProfileImageUrl;
    private String userRightName;
    private Gender userRightGender;
    private LocalDate userRightBirthday;
    private String userRightProfileImageUrl;
    private String bio;

    public static CoupleProfileResponse of(Couple couple) {
        return CoupleProfileResponse.builder()
            .coupleName(couple.getName())
            .coupleCode(couple.getCode())
            .userLeftName(couple.getUserLeft().getName())
            .userLeftGender(couple.getUserLeft().getGender())
            .userLeftBirthday(couple.getUserLeft().getBirthday())
            .userLeftProfileImageUrl(couple.getUserLeft().getProfileImageUrl())
            .userRightName(couple.getUserRight().getName())
            .userRightGender(couple.getUserRight().getGender())
            .userRightBirthday(couple.getUserRight().getBirthday())
            .userRightProfileImageUrl(couple.getUserRight().getProfileImageUrl())
            .bio(couple.getBio())
            .build();
    }
}
