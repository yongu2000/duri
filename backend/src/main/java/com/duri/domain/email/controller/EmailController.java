package com.duri.domain.email.controller;


import com.duri.domain.email.dto.SendEmailRequest;
import com.duri.domain.email.dto.VerifyCodeRequest;
import com.duri.domain.email.dto.VerifyCodeResponse;
import com.duri.domain.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send/code")
    public ResponseEntity<?> sendVerificationEmail(
        @Valid @RequestBody SendEmailRequest request) {
        emailService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/password/reset")
    public ResponseEntity<?> sendPasswordResetEmail(
        @Valid @RequestBody SendEmailRequest request) {
        emailService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify/code")
    public ResponseEntity<VerifyCodeResponse> verifyCode(
        @Valid @RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(emailService.verifyCode(request.getEmail(), request.getCode()));
    }
}
