package com.duri.domain.couple.dto.coupleprofile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CoupleProfileEditRequest {

    private String coupleName;
    private String coupleCode;
    private String bio;
}
