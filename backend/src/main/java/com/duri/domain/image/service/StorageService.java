package com.duri.domain.image.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String store(MultipartFile file);

    String downloadAndSaveImage(String imageUrl);
}
