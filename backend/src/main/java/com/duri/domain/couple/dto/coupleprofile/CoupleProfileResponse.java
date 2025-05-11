package com.duri.domain.couple.dto.coupleprofile;

import com.duri.domain.couple.entity.Couple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CoupleProfileResponse {

    private String coupleName;
    private String userLeftName;
    private String userLeftProfileImageUrl;
    private String userRightName;
    private String userRightProfileImageUrl;
    private String bio;

    public static CoupleProfileResponse of(Couple couple) {
        return CoupleProfileResponse.builder()
            .coupleName(couple.getName())
            .userLeftName(couple.getUserLeft().getName())
            .userLeftProfileImageUrl(couple.getUserLeft().getProfileImageUrl())
            .userRightName(couple.getUserRight().getName())
            .userRightProfileImageUrl(couple.getUserRight().getProfileImageUrl())
            .bio(couple.getBio())
            .build();
    }
}
