package com.duri.domain.couple.service;

import com.duri.domain.couple.dto.coupleprofile.CoupleEditProfileResponse;
import com.duri.domain.couple.dto.coupleprofile.CoupleProfileEditRequest;
import com.duri.domain.couple.dto.coupleprofile.CoupleProfileResponse;
import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.repository.CoupleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class CoupleProfileService {

    private final CoupleRepository coupleRepository;
    private final CoupleService coupleService;

    public CoupleProfileResponse getProfile(String coupleCode) {
        return CoupleProfileResponse.of(coupleService.findCoupleWithUsersByCode(coupleCode));
    }

    public CoupleEditProfileResponse getEditProfile(String coupleCode) {
        return CoupleEditProfileResponse.of(coupleService.findByCode(coupleCode));
    }

    // 자신이 Couple에 속할 때만 수정할 수 있도록 권한 설정
    public Void editProfile(String coupleCode, CoupleProfileEditRequest request) {
        Couple couple = coupleService.findCoupleWithUsersByCode(coupleCode);
        couple.changeName(request.getCoupleName());
        String newCoupleCode = request.getCoupleCode();
        if (!newCoupleCode.isEmpty() &&
            coupleRepository.findByCode(newCoupleCode).isEmpty()) {
            couple.changeCode(newCoupleCode);
            couple.getUserLeft().setCoupleCode(newCoupleCode);
            couple.getUserRight().setCoupleCode(newCoupleCode);
        }
        couple.changeBio(request.getBio());
        return null;
    }

}
