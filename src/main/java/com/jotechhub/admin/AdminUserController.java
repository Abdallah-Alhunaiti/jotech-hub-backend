package com.jotechhub.admin;

import com.jotechhub.common.response.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiSuccessResponse<List<AdminUserResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active
    ) {
        return ApiSuccessResponse.of(adminUserService.getUsers(role, active));
    }

    @GetMapping("/{userId}")
    public ApiSuccessResponse<AdminUserResponse> getUserById(@PathVariable Long userId) {
        return ApiSuccessResponse.of(adminUserService.getUserById(userId));
    }

    @PutMapping("/{userId}/deactivate")
    public ApiSuccessResponse<DeactivateUserResponse> deactivateUser(@PathVariable Long userId) {
        return ApiSuccessResponse.of(adminUserService.deactivateUser(userId));
    }
}