package com.duri.domain.couple.controller;

import com.duri.domain.couple.dto.coupleprofile.CoupleEditProfileResponse;
import com.duri.domain.couple.dto.coupleprofile.CoupleProfileEditRequest;
import com.duri.domain.couple.dto.coupleprofile.CoupleProfileResponse;
import com.duri.domain.couple.service.CoupleProfileService;
import com.duri.domain.couple.service.CoupleService;
import com.duri.global.dto.DuplicateCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/couple")
public class CoupleProfileController {

    private final CoupleProfileService coupleProfileService;
    private final CoupleService coupleService;

    @GetMapping("/profile/{coupleCode}")
    public ResponseEntity<CoupleProfileResponse> getCoupleProfile(
        @PathVariable String coupleCode
    ) {
        return ResponseEntity.ok(coupleProfileService.getProfile(coupleCode));
    }

    @GetMapping("/profile/{coupleCode}/edit")
    public ResponseEntity<CoupleEditProfileResponse> getCoupleEditProfile(
        @PathVariable String coupleCode
    ) {
        return ResponseEntity.ok(coupleProfileService.getEditProfile(coupleCode));
    }

    @PutMapping("/profile/{coupleCode}/edit")
    public ResponseEntity<Void> editCoupleProfile(
        @PathVariable String coupleCode,
        @RequestBody CoupleProfileEditRequest request
    ) {
        return ResponseEntity.ok(coupleProfileService.editProfile(coupleCode, request));
    }

    @GetMapping("/check/code/{coupleCode}")
    public ResponseEntity<DuplicateCheckResponse> checkCoupleCodeDuplicate(
        @PathVariable String coupleCode) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(coupleService.checkCoupleCodeDuplicate(coupleCode));
    }

}
