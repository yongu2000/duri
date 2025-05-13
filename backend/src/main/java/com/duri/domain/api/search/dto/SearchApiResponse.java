package com.duri.domain.api.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchApiResponse {

    private String placeName;
    private String placeUrl;
    private String category;
    private String address;
    private String roadAddress;
    private String phone;
    private Double x;
    private Double y;
}
