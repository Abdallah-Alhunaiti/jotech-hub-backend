package com.jotechhub.savedevent;

import com.jotechhub.common.response.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/saved-events")
@RequiredArgsConstructor
public class SavedEventController {

    private final SavedEventService savedEventService;

    @PostMapping("/{eventId}")
    public ApiSuccessResponse<SavedEventResponse> saveEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(savedEventService.saveEvent(eventId, authentication));
    }

    @DeleteMapping("/{eventId}")
    public ApiSuccessResponse<Map<String, String>> unsaveEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        savedEventService.unsaveEvent(eventId, authentication);
        return ApiSuccessResponse.of(Map.of("message", "Event removed from saved events successfully"));
    }

    @GetMapping
    public ApiSuccessResponse<List<SavedEventResponse>> getMySavedEvents(Authentication authentication) {
        return ApiSuccessResponse.of(savedEventService.getMySavedEvents(authentication));
    }
}