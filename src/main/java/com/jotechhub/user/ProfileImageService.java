package com.jotechhub.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileImageService {

    private final UserRepository userRepository;

    @Value("${file.profile-images-dir}")
    private String profileImagesDir;

    @Value("${app.base-url}")
    private String appBaseUrl;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public ProfileImageResponse uploadProfileImage(
            MultipartFile file,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);

        validateFile(file);

        try {
            Path uploadPath = Paths.get(profileImagesDir);
            Files.createDirectories(uploadPath);

            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = "user-" + user.getId() + "-" + UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            deleteOldImageIfExists(user.getProfileImageUrl());

            String imageUrl = appBaseUrl + "/uploads/profile-images/" + fileName;

            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);

            return ProfileImageResponse.builder()
                    .userId(user.getId())
                    .profileImageUrl(imageUrl)
                    .message("Profile image uploaded successfully")
                    .build();

        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not upload profile image"
            );
        }
    }

    public ProfileImageResponse deleteProfileImage(Authentication authentication) {
        User user = getCurrentUser(authentication);

        deleteOldImageIfExists(user.getProfileImageUrl());

        user.setProfileImageUrl(null);
        userRepository.save(user);

        return ProfileImageResponse.builder()
                .userId(user.getId())
                .profileImageUrl(null)
                .message("Profile image deleted successfully")
                .build();
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is deactivated");
        }

        return user;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile image is required");
        }

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
                    "Profile image must not exceed 2MB"
            );
        }
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

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

    private void deleteOldImageIfExists(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path oldPath = Paths.get(profileImagesDir).resolve(fileName);
            Files.deleteIfExists(oldPath);
        } catch (Exception ignored) {
            // لا نوقف العملية إذا فشل حذف الصورة القديمة
        }
    }
}