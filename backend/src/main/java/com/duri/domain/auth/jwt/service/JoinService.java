package com.duri.domain.auth.jwt.service;

import com.duri.domain.auth.exception.DuplicateUserException;
import com.duri.domain.auth.exception.EmailNotVerifiedException;
import com.duri.domain.auth.jwt.dto.JoinRequest;
import com.duri.domain.auth.jwt.dto.JoinResponse;
import com.duri.domain.user.entity.Role;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class JoinService {

    private static final String EMAIL_VERIFIED_KEY = "EMAIL:VERIFIED:";

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinResponse join(JoinRequest request) {
        validateEmailExistence(request.getEmail());
        validateEmailVerification(request.getEmail());

        String username = generateUniqueUsername(request.getEmail());
        String name = parseEmailToUsername(request.getEmail());

        User newUser = User.builder()
            .email(request.getEmail())
            .username(username)
            .password(bCryptPasswordEncoder.encode(request.getPassword()))
            .name(name)
            .role(Role.USER)
            .build();
        userRepository.save(newUser);
        return JoinResponse.builder()
            .id(newUser.getId())
            .username(newUser.getUsername())
            .build();
    }

    private void validateEmailVerification(String email) {
        String value = redisTemplate.opsForValue().get(EMAIL_VERIFIED_KEY + email);
        if (!Boolean.parseBoolean(value)) {
            throw new EmailNotVerifiedException();
        }
    }

    private void validateEmailExistence(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new DuplicateUserException();
        }
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = parseEmailToUsername(email);
        String username = baseUsername;
        int randomNumber;

        while (userRepository.findByUsername(username).isPresent()) {
            randomNumber = (int) (Math.random() * 10000);
            username = baseUsername + randomNumber;
        }

        return username;
    }

    private String parseEmailToUsername(String email) {
        return email.split("@")[0];
    }

}
