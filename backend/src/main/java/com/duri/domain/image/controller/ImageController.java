package com.duri.domain.image.controller;

import com.duri.domain.image.dto.ImageUploadResponse;
import com.duri.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponse> uploadImage(
        @RequestPart MultipartFile imageFile
    ) {
        return ResponseEntity.ok(new ImageUploadResponse(imageService.upload(imageFile)));
    }

    @DeleteMapping("/delete/{imageUrl}")
    public ResponseEntity<Void> deleteImage(
        @PathVariable String imageUrl
    ) {
        imageService.delete(imageUrl);
        return ResponseEntity.ok().build();
    }
}
