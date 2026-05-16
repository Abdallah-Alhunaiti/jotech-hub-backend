package com.jotechhub.event;

import com.jotechhub.common.response.ApiSuccessResponse;
import com.jotechhub.subscription.EventSubscriberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService adminEventService;

    @GetMapping
    public ApiSuccessResponse<List<AdminEventResponse>> getAllEvents(
            @RequestParam(required = false) String status
    ) {
        return ApiSuccessResponse.of(adminEventService.getAllEvents(status));
    }

    @GetMapping("/{eventId}")
    public ApiSuccessResponse<AdminEventResponse> getEventById(@PathVariable Long eventId) {
        return ApiSuccessResponse.of(adminEventService.getEventById(eventId));
    }

    @PutMapping("/{eventId}/approve")
    public ApiSuccessResponse<AdminEventResponse> approveEvent(@PathVariable Long eventId) {
        return ApiSuccessResponse.of(adminEventService.approveEvent(eventId));
    }

    @PutMapping("/{eventId}/reject")
    public ApiSuccessResponse<AdminEventResponse> rejectEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody RejectEventRequest request
    ) {
        return ApiSuccessResponse.of(adminEventService.rejectEvent(eventId, request));
    }
    @DeleteMapping("/{eventId}")
    public ApiSuccessResponse<AdminEventResponse> deleteEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody(required = false) AdminDeleteEventRequest request
    ) {
        return ApiSuccessResponse.of(adminEventService.deleteEvent(eventId, request));
    }

    @GetMapping("/{eventId}/subscribers")
    public ApiSuccessResponse<List<EventSubscriberResponse>> getEventSubscribers(
            @PathVariable Long eventId
    ) {
        return ApiSuccessResponse.of(adminEventService.getEventSubscribers(eventId));
    }
}
