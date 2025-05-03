package com.duri.domain.image.exception;


import com.duri.global.exception.BusinessException;

public class ImageUploadFailException extends BusinessException {

    public ImageUploadFailException() {
        super(ImageError.IMAGE_UPLOAD_FAIL);
    }
}
