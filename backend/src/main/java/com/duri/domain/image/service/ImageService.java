package com.duri.domain.image.service;

import com.duri.domain.image.entity.Image;
import com.duri.domain.image.exception.ImageNotFoundException;
import com.duri.domain.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private final StorageService storageService;
    private final ImageRepository imageRepository;

    public String upload(MultipartFile file) {
        String url = storageService.store(file);
        save(url);
        return url;
    }

    public String saveExternalImage(String imageUrl) {
        String url = storageService.downloadAndSaveImage(imageUrl);
        save(url);
        return url;
    }

    private void save(String url) {
        imageRepository.save(Image.builder()
            .url(url)
            .build());
    }

    public void delete(String url) {
        imageRepository.deleteByUrl(url);
    }

    public Image findByUrl(String url) {
        return imageRepository.findByUrl(url).orElseThrow(ImageNotFoundException::new);
    }
}
