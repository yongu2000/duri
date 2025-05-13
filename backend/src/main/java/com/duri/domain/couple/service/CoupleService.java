package com.duri.domain.couple.service;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.exception.InvalidCoupleCodeException;
import com.duri.domain.couple.repository.CoupleRepository;
import com.duri.global.dto.DuplicateCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class CoupleService {

    private final CoupleRepository coupleRepository;

    public DuplicateCheckResponse checkCoupleCodeDuplicate(String coupleCode) {
        if (coupleRepository.findByCode(coupleCode).isPresent()) {
            return new DuplicateCheckResponse(true);
        }
        return new DuplicateCheckResponse(false);
    }

    public Couple findCoupleWithUsersByCode(String coupleCode) {
        return coupleRepository.findCoupleWithUsersByCode(coupleCode)
            .orElseThrow(InvalidCoupleCodeException::new);
    }

    public Couple findByCode(String coupleCode) {
        return coupleRepository.findByCode(coupleCode)
            .orElseThrow(InvalidCoupleCodeException::new);
    }
}
