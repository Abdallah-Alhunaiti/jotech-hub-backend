package com.jotechhub.user;

import com.jotechhub.common.response.ApiSuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping
    public ApiSuccessResponse<StudentProfileResponse> getMyProfile(
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(
                studentProfileService.getMyProfile(authentication)
        );
    }

    @PutMapping
    public ApiSuccessResponse<StudentProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateStudentProfileRequest request,
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(
                studentProfileService.updateMyProfile(request, authentication)
        );
    }
}