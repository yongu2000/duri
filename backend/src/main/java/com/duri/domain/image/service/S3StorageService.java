package com.duri.domain.image.service;

import com.duri.domain.image.exception.ImageUploadFailException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Profile("prod")
@Service
@Transactional
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.url-prefix}")
    private String urlPrefix;

    @Override
    public String store(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            uploadToS3(file.getBytes(), fileName, file.getContentType());
            return urlPrefix + fileName;
        } catch (IOException e) {
            throw new ImageUploadFailException();
        }
    }

    @Override
    public String downloadAndSaveImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            String contentType = conn.getContentType();
            String extension = getExtensionFromContentType(contentType);
            String originalFileName = getFileNameFromUrl(imageUrl);

            if (originalFileName.isBlank()) {
                originalFileName = "unknown" + extension;
            }

            String fileName = UUID.randomUUID() + "_" + originalFileName + extension;
            try (InputStream in = conn.getInputStream()) {
                byte[] imageBytes = in.readAllBytes();
                uploadToS3(imageBytes, fileName, contentType);
            }

            return urlPrefix + fileName;
        } catch (IOException e) {
            throw new ImageUploadFailException();
        }
    }

    private void uploadToS3(byte[] fileBytes, String fileName, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
//            .acl("public-read")
            .key(fileName)
            .contentType(contentType)
            .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
    }

    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String getFileNameFromUrl(String imageUrl) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        return fileName;
    }
}