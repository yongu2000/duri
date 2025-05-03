package com.duri.domain.image.service;

import com.duri.domain.image.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private final StorageService storageService;

    public ImageUploadResponse uploadImage(MultipartFile file) {
        String url = storageService.store(file);
        return new ImageUploadResponse(url);
    }

    public String saveExternalImage(String imageUrl) {
        return storageService.downloadAndSaveImage(imageUrl);
    }
}
