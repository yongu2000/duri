package com.duri.domain.couple.dto.coupleprofile;

import com.duri.domain.couple.entity.Couple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CoupleEditProfileResponse {

    private String coupleName;
    private String coupleCode;
    private String bio;

    public static CoupleEditProfileResponse of(Couple couple) {
        return CoupleEditProfileResponse.builder()
            .coupleName(couple.getName())
            .coupleCode(couple.getCode())
            .bio(couple.getBio())
            .build();
    }
}
