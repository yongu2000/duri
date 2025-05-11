package com.duri.domain.couple.service;

import com.duri.domain.couple.dto.coupleprofile.CoupleProfileResponse;
import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.exception.InvalidCoupleCodeException;
import com.duri.domain.couple.repository.CoupleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CoupleProfileService {

    private final CoupleRepository coupleRepository;

    public CoupleProfileResponse getProfile(String coupleCode) {
        Couple couple = coupleRepository.findByCoupleCode(coupleCode)
            .orElseThrow(InvalidCoupleCodeException::new);

        return CoupleProfileResponse.of(couple);
    }
}
