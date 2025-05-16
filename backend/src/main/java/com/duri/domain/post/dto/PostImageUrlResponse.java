package com.duri.domain.post.dto;

import com.duri.domain.image.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostImageUrlResponse {

    private String imageUrl;

    public static PostImageUrlResponse from(Image image) {
        return new PostImageUrlResponse(image.getUrl());
    }
}
