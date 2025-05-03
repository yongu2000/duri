package com.duri.domain.user.service;

import static com.duri.domain.email.constant.EmailRedisKey.PASSWORD_RESET_TOKEN_KEY;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.auth.jwt.exception.UserDetailNotFoundException;
import com.duri.domain.user.dto.EmailDuplicateCheckResponse;
import com.duri.domain.user.dto.PasswordResetRequest;
import com.duri.domain.user.dto.UserResponse;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.exception.PasswordResetTokenNotMatchException;
import com.duri.domain.user.exception.UserNotFoundException;
import com.duri.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public EmailDuplicateCheckResponse checkEmailDuplicate(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            return new EmailDuplicateCheckResponse(true);
        }
        return new EmailDuplicateCheckResponse(false);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    public UserResponse getUserProfile() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            User user = findByUsername(userDetails.getUsername()); // DB 조회
            return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
        } else {
            throw new UserDetailNotFoundException();
        }
    }

    public void resetUserPassword(PasswordResetRequest request) {
        String email = redisTemplate.opsForValue()
            .get(PASSWORD_RESET_TOKEN_KEY + request.getToken());
        if (email == null) {
            throw new PasswordResetTokenNotMatchException();
        }
        User user = userRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);
        user.updatePassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        redisTemplate.delete(PASSWORD_RESET_TOKEN_KEY + request.getToken());
    }
}
