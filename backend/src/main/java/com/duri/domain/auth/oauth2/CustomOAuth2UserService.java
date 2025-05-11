package com.duri.domain.auth.oauth2;


import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.image.service.ImageService;
import com.duri.domain.user.entity.Role;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ImageService imageService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. OAuth2 로그인 유저 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("getAttributes : {}", oAuth2User.getAttributes());

        // 2. provider : kakao, naver, google
        String provider = userRequest.getClientRegistration().getRegistrationId();
        log.info("provider : {}", provider);

        // 3. 필요한 정보를 provider에 따라 다르게 mapping
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(provider, oAuth2User.getAttributes());
        log.info("oAuth2UserInfo : {}", oAuth2UserInfo.toString());

        String username = generateUniqueUsername(oAuth2UserInfo.getEmail());
        User user = getUser(username, oAuth2UserInfo, provider);
        log.info("user : {}", user);

        // 5. UserDetails와 OAuth2User를 다중 상속한 CustomUserDetails
        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }

    private User getUser(String username, OAuth2UserInfo oAuth2UserInfo, String provider) {
        return userRepository.findByEmail(oAuth2UserInfo.getEmail())
            .orElseGet(() -> {
                String imageUrl = imageService.saveExternalImage(
                    oAuth2UserInfo.getProfileImageUrl());
                User newUser = User.builder()
                    .username(username)
                    .password(oAuth2UserInfo.getPassword())
                    .name(oAuth2UserInfo.getName())
                    .email(oAuth2UserInfo.getEmail())
                    .role(Role.USER)
                    .birthday(oAuth2UserInfo.getBirthday())
                    .gender(oAuth2UserInfo.getGender())
                    .profileImageUrl(imageUrl)
                    .provider(provider)
                    .build();
                return userRepository.save(newUser);
            });
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int randomNumber = 0;

        while (userRepository.findByUsername(username).isPresent()) {
            randomNumber = (int) (Math.random() * 10000);
            username = baseUsername + randomNumber;
        }

        return username;
    }
}
