package com.duri.global.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DuplicateCheckResponse {

    private boolean isDuplicate;

    @JsonProperty("isDuplicate")
    public boolean isDuplicate() {
        return isDuplicate;
    }
}
