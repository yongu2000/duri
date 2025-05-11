package com.duri.domain.couple.controller;

import com.duri.domain.couple.dto.coupleprofile.CoupleProfileResponse;
import com.duri.domain.couple.service.CoupleProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/couple")
public class CoupleProfileController {

    private final CoupleProfileService coupleProfileService;

    @GetMapping("/profile/{coupleCode}")
    public ResponseEntity<CoupleProfileResponse> getUserCode(
        @PathVariable String coupleCode
    ) {
        return ResponseEntity.ok(coupleProfileService.getProfile(coupleCode));
    }

}
