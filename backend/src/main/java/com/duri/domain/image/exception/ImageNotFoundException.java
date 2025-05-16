package com.duri.domain.image.exception;

import com.duri.global.exception.BusinessException;

public class ImageNotFoundException extends BusinessException {

    public ImageNotFoundException() {
        super(ImageError.IMAGE_UPLOAD_FAIL);
    }
}
