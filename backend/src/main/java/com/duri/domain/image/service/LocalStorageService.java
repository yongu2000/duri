package com.duri.domain.image.service;

import com.duri.domain.image.exception.ImageUploadFailException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Profile("dev")
@Service
@Transactional
public class LocalStorageService implements StorageService {

    private static final String baseDir = "uploads/image/";
    private static final String fileUploadPath = "/uploads/image/";

    @Override
    public String store(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            saveFile(file.getBytes(), fileName);
            return fileUploadPath + fileName;
        } catch (IOException e) {
            throw new ImageUploadFailException();
        }
    }

    @Override
    public String downloadAndSaveImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl); // URL 가져오기
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect(); // URL 접속

            String contentType = conn.getContentType(); // contentType 가져오기
            String extension = getExtensionFromContentType(contentType); // 확장자명 결정

            String originalFileName = getFileNameFromUrl(imageUrl); // 파일명 파싱
            if (originalFileName.isBlank()) { // 파일명 없으면 unknown.확장자
                originalFileName = "unknown" + extension;
            }

            // 파일명: UUID + originalName + 확장자
            String fileName = UUID.randomUUID() + originalFileName + extension;
            // 디렉토리에 파일명으로 저장
            try (InputStream in = conn.getInputStream()) { // InputStream을 열어서 conn에서 이미지 데이터 가져옴
                byte[] imageBytes = in.readAllBytes(); // imageBytes에 이미지 이진 데이터 담겨있음
                saveFile(imageBytes, fileName); // 로컬 디스크에 파일 저장
            }

            return fileUploadPath + fileName;

        } catch (IOException e) {
            throw new ImageUploadFailException();
        }
    }

    private void saveFile(byte[] fileBytes, String fileName) throws IOException {
        Path path = Paths.get(baseDir, fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, fileBytes);
    }

    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg"; // 기본 확장자
        };
    }

    private String getFileNameFromUrl(String imageUrl) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?")); // 쿼리스트링 제거
        }
        return fileName;
    }
}
