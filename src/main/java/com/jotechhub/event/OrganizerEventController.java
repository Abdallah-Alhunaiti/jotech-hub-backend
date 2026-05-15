package com.jotechhub.event;

import com.jotechhub.common.response.ApiSuccessResponse;
import com.jotechhub.subscription.EventSubscriberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizer/events")
@RequiredArgsConstructor
public class OrganizerEventController {

    private final OrganizerEventService organizerEventService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<OrganizerEventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication
    ) {
        OrganizerEventResponse response = organizerEventService.createEvent(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of(response));
    }

    @GetMapping
    public ApiSuccessResponse<List<OrganizerEventResponse>> getMyEvents(Authentication authentication) {
        return ApiSuccessResponse.of(organizerEventService.getMyEvents(authentication));
    }

    @GetMapping("/{eventId}")
    public ApiSuccessResponse<OrganizerEventResponse> getMyEventById(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(organizerEventService.getMyEventById(eventId, authentication));
    }

    @PutMapping("/{eventId}")
    public ApiSuccessResponse<OrganizerEventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequest request,
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(organizerEventService.updateEvent(eventId, request, authentication));
    }

    @DeleteMapping("/{eventId}")
    public ApiSuccessResponse<OrganizerEventResponse> deleteEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(organizerEventService.deleteEvent(eventId, authentication));
    }

    @GetMapping("/{eventId}/subscribers")
    public ApiSuccessResponse<List<EventSubscriberResponse>> getEventSubscribers(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ApiSuccessResponse.of(organizerEventService.getEventSubscribers(eventId, authentication));
    }
}