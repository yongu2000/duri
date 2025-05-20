package com.duri.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingPostCountResponse {

    private Long count;

    public static PendingPostCountResponse from(Long count) {
        return new PendingPostCountResponse(count);
    }
}
