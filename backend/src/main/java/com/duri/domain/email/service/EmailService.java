package com.duri.domain.email.service;


import static com.duri.domain.email.constant.EmailRedisKey.EMAIL_VERIFICATION_KEY;
import static com.duri.domain.email.constant.EmailRedisKey.EMAIL_VERIFIED_KEY;
import static com.duri.domain.email.constant.EmailRedisKey.PASSWORD_RESET_TOKEN_KEY;

import com.duri.domain.email.dto.VerifyCodeResponse;
import com.duri.domain.email.exception.EmailSendFailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender mailSender;
    @Value("${app.frontend.url}")
    private String FRONTEND_URL;

    public void sendVerificationEmail(String email) {
        String code = createVerificationCode();
        saveVerificationCode(email, code);
        sendVerificationHtmlEmail(email, code);
    }

    public void sendPasswordResetEmail(String email) {
        String token = UUID.randomUUID().toString();
        savePasswordResetToken(email, token);
        sendPasswordResetHtmlEmail(email, token);
    }

    private void sendVerificationHtmlEmail(String to, String code) {
        String htmlContent =
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "<div style='background-color: #f8f9fa; border-radius: 8px; padding: 20px; text-align: center;'>"
                + "<h2 style='color: #333; margin-bottom: 20px;'>이메일 인증</h2>"
                + "<div style='background-color: #e9ecef; padding: 15px; border-radius: 6px; margin: 20px 0;'>"
                + "<p style='font-size: 24px; font-weight: bold; color: #495057; margin: 0;'>"
                + code + "</p>"
                + "</div>"
                + "<p style='color: #666; margin: 10px 0;'>위 인증 코드를 입력해주세요.</p>"
                + "<p style='color: #dc3545; font-size: 14px;'>유효 시간: 5분</p>"
                + "</div>"
                + "<div style='text-align: center; margin-top: 20px;'>"
                + "<p style='color: #999; font-size: 12px;'>본 메일은 발신 전용입니다.</p>"
                + "</div>"
                + "</div>";

        sendHtmlEmail(to, "이메일 인증 코드", htmlContent);
    }

    private void sendPasswordResetHtmlEmail(String to, String token) {
        String resetLink = FRONTEND_URL + "/reset-password?token=" + token;
        String htmlContent =
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "<div style='background-color: #f8f9fa; border-radius: 8px; padding: 20px; text-align: center;'>"
                + "<h2 style='color: #333;'>비밀번호 재설정</h2>"
                + "<p style='margin: 20px 0;'>아래 버튼을 클릭하여 비밀번호를 재설정하세요.</p>"
                + "<a href='" + resetLink
                + "' style='display: inline-block; padding: 10px 20px; background-color: #007bff; color: #fff;"
                + " text-decoration: none; border-radius: 4px;'>비밀번호 재설정</a>"
                + "<p style='margin-top: 20px; font-size: 14px; color: #dc3545;'>유효 시간: 1시간</p>"
                + "</div>"
                + "<div style='text-align: center; margin-top: 20px;'>"
                + "<p style='color: #999; font-size: 12px;'>본 메일은 발신 전용입니다.</p>"
                + "</div>"
                + "</div>";

        sendHtmlEmail(to, "비밀번호 재설정 링크", htmlContent);
    }

    @Async
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // HTML 형식
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendFailException();
        }
    }

    private void savePasswordResetToken(String email, String token) {
        redisTemplate.opsForValue().set(
            PASSWORD_RESET_TOKEN_KEY + token,
            email,
            Duration.ofHours(1)
        );
    }

    private String createVerificationCode() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    private void saveVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(
            EMAIL_VERIFICATION_KEY + email,
            String.valueOf(code),
            Duration.ofMinutes(5)
        );
    }

    public VerifyCodeResponse verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue()
            .get(EMAIL_VERIFICATION_KEY + email);
        boolean verified = code.equals(storedCode);
        if (verified) {
            redisTemplate.opsForValue().set(
                EMAIL_VERIFIED_KEY + email,
                String.valueOf(true),
                Duration.ofMinutes(10)
            );
        }
        return new VerifyCodeResponse(code.equals(storedCode));
    }


}
