package com.jotechhub.user;

import com.jotechhub.common.response.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me/profile-image")
@RequiredArgsConstructor
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @PutMapping
    public ApiSuccessResponse<ProfileImageResponse> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(
                profileImageService.uploadProfileImage(file, authentication)
        );
    }

    @DeleteMapping
    public ApiSuccessResponse<ProfileImageResponse> deleteProfileImage(
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(
                profileImageService.deleteProfileImage(authentication)
        );
    }
}