package com.jotechhub.event;

import com.jotechhub.common.response.ApiSuccessResponse;
import com.jotechhub.common.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/public/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final PublicEventService publicEventService;

    @GetMapping
    public ApiSuccessResponse<PagedResponse<PublicEventCardResponse>> getPublicEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "upcoming") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiSuccessResponse.of(
                publicEventService.getPublicEvents(
                        keyword,
                        categoryId,
                        cityId,
                        tagId,
                        location,
                        dateFrom,
                        dateTo,
                        sortBy,
                        page,
                        size
                )
        );
    }

    @GetMapping("/upcoming")
    public ApiSuccessResponse<List<PublicEventCardResponse>> getUpcomingEvents() {
        return ApiSuccessResponse.of(publicEventService.getUpcomingEvents());
    }

    @GetMapping("/recent")
    public ApiSuccessResponse<List<PublicEventCardResponse>> getRecentEvents() {
        return ApiSuccessResponse.of(publicEventService.getRecentEvents());
    }

    @GetMapping("/{eventId}")
    public ApiSuccessResponse<PublicEventDetailsResponse> getEventDetails(@PathVariable Long eventId) {
        return ApiSuccessResponse.of(publicEventService.getEventDetails(eventId));
    }
}