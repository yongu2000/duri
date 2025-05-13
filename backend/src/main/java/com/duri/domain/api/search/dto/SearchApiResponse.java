package com.duri.domain.api.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchApiResponse {

    private String placeName;
    private String addressName;
}
