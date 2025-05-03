package com.duri.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EmailDuplicateCheckResponse {

    private boolean isDuplicate;

    @JsonProperty("isDuplicate")
    public boolean isDuplicate() {
        return isDuplicate;
    }
}
