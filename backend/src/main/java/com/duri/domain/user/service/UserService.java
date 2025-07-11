package com.duri.domain.user.service;

import static com.duri.domain.email.constant.EmailRedisKey.PASSWORD_RESET_TOKEN_KEY;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.auth.exception.UserDetailNotFoundException;
import com.duri.domain.user.dto.PasswordResetRequest;
import com.duri.domain.user.dto.UserProfileEditRequest;
import com.duri.domain.user.dto.UserResponse;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.exception.PasswordResetTokenNotMatchException;
import com.duri.domain.user.exception.UserNotFoundException;
import com.duri.domain.user.repository.UserRepository;
import com.duri.global.dto.DuplicateCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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

    public DuplicateCheckResponse checkEmailDuplicate(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            return new DuplicateCheckResponse(true);
        }
        return new DuplicateCheckResponse(false);
    }

    public DuplicateCheckResponse checkUsernameDuplicate(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            return new DuplicateCheckResponse(true);
        }
        return new DuplicateCheckResponse(false);
    }

    @Cacheable(value = "USER:findById", key = "#userId")
    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
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
            return UserResponse.from(user);
        } else {
            throw new UserDetailNotFoundException();
        }
    }

    // 자기 자신 OR Admin만 수정할 수 있도록 권한 부여
//    @CacheEvict(value = "USER:findById", key = "#userId") 리팩토링 필요
    public void editUserProfile(String username, UserProfileEditRequest request) {
        User user = findByUsername(username);
        user.updateName(request.getName());
        user.updateGender(request.getGender());
        user.updateBirthday(request.getBirthday());
        user.updateProfileImageUrl(request.getProfileImageUrl());
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
