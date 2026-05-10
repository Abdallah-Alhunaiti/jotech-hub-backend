package com.jotechhub.organizer;

import com.jotechhub.common.response.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizer/dashboard")
@RequiredArgsConstructor
public class OrganizerDashboardController {

    private final OrganizerDashboardService organizerDashboardService;

    @GetMapping
    public ApiSuccessResponse<OrganizerDashboardResponse> getDashboard(Authentication authentication) {
        return ApiSuccessResponse.of(organizerDashboardService.getDashboard(authentication));
    }
}