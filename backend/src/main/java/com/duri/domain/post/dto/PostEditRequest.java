package com.duri.domain.post.dto;

import com.duri.domain.post.constant.Scope;
import com.duri.global.util.AESUtil;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostEditRequest {

    private String idToken;

    private String placeName;
    private String title;
    private String placeUrl;
    private String category;
    private String phone;
    private String address;
    private String roadAddress;
    private Double x;
    private Double y;

    private LocalDate date;
    private Integer rate;
    private String comment;

    private Scope scope;

    private List<String> imageUrls;

    public Long getPostId() {
        return Long.parseLong(AESUtil.decrypt(idToken));
    }

}
