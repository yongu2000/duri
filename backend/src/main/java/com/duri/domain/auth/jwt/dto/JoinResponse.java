package com.duri.domain.auth.jwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinResponse {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("username")
    private String username;

}
