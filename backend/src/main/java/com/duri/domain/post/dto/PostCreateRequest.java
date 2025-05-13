package com.duri.domain.post.dto;

import com.duri.domain.post.constant.Scope;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostCreateRequest {

    private String title;

    private String placeName;
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

}
