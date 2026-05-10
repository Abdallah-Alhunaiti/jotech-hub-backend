package com.jotechhub.event;

import com.jotechhub.common.response.PagedResponse;
import com.jotechhub.organizer.OrganizerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventService {

    private final EventRepository eventRepository;
    private final OrganizerProfileRepository organizerProfileRepository;

    public PagedResponse<PublicEventCardResponse> getPublicEvents(
            String keyword,
            Long categoryId,
            Long tagId,
            String location,
            LocalDate dateFrom,
            LocalDate dateTo,
            String sortBy,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                buildSort(sortBy)
        );

        Page<Event> eventPage = eventRepository.searchPublicEvents(
                normalizeText(keyword),
                categoryId,
                tagId,
                normalizeText(location),
                dateFrom,
                dateTo,
                pageable
        );

        List<PublicEventCardResponse> content = eventPage.getContent()
                .stream()
                .map(this::mapToCardResponse)
                .toList();

        return PagedResponse.<PublicEventCardResponse>builder()
                .content(content)
                .page(eventPage.getNumber())
                .size(eventPage.getSize())
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .last(eventPage.isLast())
                .build();
    }

    public PublicEventDetailsResponse getEventDetails(Long eventId) {
        Event event = eventRepository.findByIdAndStatusAndCancelledFalse(eventId, EventStatus.APPROVED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        return mapToDetailsResponse(event);
    }

    public List<PublicEventCardResponse> getUpcomingEvents() {
        return eventRepository
                .findTop6ByStatusAndCancelledFalseAndEventDateGreaterThanEqualOrderByEventDateAscEventTimeAsc(
                        EventStatus.APPROVED,
                        LocalDate.now()
                )
                .stream()
                .map(this::mapToCardResponse)
                .toList();
    }

    public List<PublicEventCardResponse> getRecentEvents() {
        return eventRepository
                .findTop6ByStatusAndCancelledFalseOrderByCreatedAtDesc(EventStatus.APPROVED)
                .stream()
                .map(this::mapToCardResponse)
                .toList();
    }

    private PublicEventCardResponse mapToCardResponse(Event event) {
        return PublicEventCardResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .organizerName(resolveOrganizerName(event.getOrganizer().getId()))
                .categoryName(event.getCategory().getName())
                .eventDate(event.getEventDate())
                .eventTime(event.getEventTime())
                .location(event.getLocation())
                .price(event.getPrice())
                .timeState(resolveTimeState(event))
                .build();
    }

    private PublicEventDetailsResponse mapToDetailsResponse(Event event) {
        return PublicEventDetailsResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .price(event.getPrice())
                .organizerName(resolveOrganizerName(event.getOrganizer().getId()))
                .categoryId(event.getCategory().getId())
                .categoryName(event.getCategory().getName())
                .eventDate(event.getEventDate())
                .eventTime(event.getEventTime())
                .location(event.getLocation())
                .registrationLink(event.getRegistrationLink())
                .capacity(event.getCapacity())
                .tags(event.getTags().stream().map(tag -> tag.getName()).sorted().toList())
                .timeState(resolveTimeState(event))
                .build();
    }

    private String resolveOrganizerName(Long userId) {
        return organizerProfileRepository.findByUserId(userId)
                .map(profile -> profile.getOrganizationName())
                .orElse("Organizer");
    }

    private String resolveTimeState(Event event) {
        return event.getEventDate().isBefore(LocalDate.now()) ? "PAST" : "UPCOMING";
    }

    private String normalizeText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }

    private Sort buildSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "eventDate", "eventTime");
        }

        return switch (sortBy.trim().toLowerCase()) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "upcoming" -> Sort.by(Sort.Direction.ASC, "eventDate", "eventTime");
            default -> Sort.by(Sort.Direction.ASC, "eventDate", "eventTime");
        };
    }
}