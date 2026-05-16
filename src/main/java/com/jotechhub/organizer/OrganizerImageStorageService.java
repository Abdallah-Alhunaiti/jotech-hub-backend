package com.jotechhub.organizer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizerImageStorageService {

    @Value("${file.organizer-images-dir}")
    private String organizerImagesDir;

    @Value("${app.base-url}")
    private String appBaseUrl;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public String storeOrganizerImage(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        validateFile(file);

        try {
            Path uploadPath = Paths.get(organizerImagesDir);
            Files.createDirectories(uploadPath);

            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = "organizer-" + userId + "-" + UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return appBaseUrl + "/uploads/organizers/" + fileName;

        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not upload organizer image"
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPG, PNG, and WEBP images are allowed"
            );
        }

        long maxSize = 2 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Organizer image must not exceed 2MB"
            );
        }
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }

        String extension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase();

        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".webp")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid image extension"
            );
        }

        return extension;
    }
}