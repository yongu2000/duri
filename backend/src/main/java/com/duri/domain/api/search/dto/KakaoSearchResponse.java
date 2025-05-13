package com.duri.domain.api.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoSearchResponse {

    private List<Document> documents;

    @Data
    public static class Document {

        @JsonProperty("place_name")
        private String placeName;

        @JsonProperty("address_name")
        private String addressName;

    }
}
