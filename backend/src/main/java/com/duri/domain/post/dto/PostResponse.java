package com.duri.domain.post.dto;

import com.duri.domain.post.constant.Scope;
import com.duri.domain.post.entity.Post;
import com.duri.domain.user.entity.Gender;
import com.duri.global.util.AESUtil;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PostResponse {

    private String idToken;
    private String title;
    private String placeName;
    private String address;

    private String category;

    private LocalDate date;

    private Double rate;

    private String userLeftProfileImageUrl;
    private Gender userLeftGender;
    private LocalDate userLeftBirthday;
    private String userLeftName;
    private Integer userLeftRate;
    private String userLeftComment;

    private String userRightProfileImageUrl;
    private Gender userRightGender;
    private LocalDate userRightBirthday;
    private String userRightName;
    private Integer userRightRate;
    private String userRightComment;

    private String coupleCode;
    private String coupleName;

    private Scope scope;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
            .idToken(AESUtil.encrypt(post.getId().toString()))
            .title(post.getTitle())
            .placeName(post.getPlaceName())
            .address(post.getAddress())
            .category(post.getCategory())
            .date(post.getDate())
            .rate(post.getRate())

            .userLeftProfileImageUrl(post.getCouple().getUserLeft().getProfileImageUrl())
            .userLeftGender(post.getCouple().getUserLeft().getGender())
            .userLeftBirthday(post.getCouple().getUserLeft().getBirthday())
            .userLeftName(post.getCouple().getUserLeft().getName())
            .userLeftRate(post.getUserLeftRate())
            .userLeftComment(post.getUserLeftComment())

            .userRightProfileImageUrl(post.getCouple().getUserRight().getProfileImageUrl())
            .userRightGender(post.getCouple().getUserRight().getGender())
            .userRightBirthday(post.getCouple().getUserRight().getBirthday())
            .userRightName(post.getCouple().getUserRight().getName())
            .userRightRate(post.getUserRightRate())
            .userRightComment(post.getUserRightComment())

            .coupleCode(post.getCouple().getCode())
            .coupleName(post.getCouple().getName())

            .scope(post.getScope())

            .build();
    }

}
