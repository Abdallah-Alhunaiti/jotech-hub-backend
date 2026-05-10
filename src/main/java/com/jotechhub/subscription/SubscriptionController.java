package com.jotechhub.subscription;

import com.jotechhub.common.response.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/{eventId}")
    public ApiSuccessResponse<SubscriptionResponse> subscribeToEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(subscriptionService.subscribeToEvent(eventId, authentication));
    }

    @DeleteMapping("/{eventId}")
    public ApiSuccessResponse<SubscriptionResponse> cancelSubscription(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(subscriptionService.cancelSubscription(eventId, authentication));
    }

    @GetMapping
    public ApiSuccessResponse<List<SubscriptionResponse>> getMySubscriptions(Authentication authentication) {
        return ApiSuccessResponse.of(subscriptionService.getMySubscriptions(authentication));
    }
}