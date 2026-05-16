package com.jotechhub.event;

import com.jotechhub.organizer.OrganizerProfileRepository;
import com.jotechhub.subscription.EventSubscriberResponse;
import com.jotechhub.subscription.Subscription;
import com.jotechhub.subscription.SubscriptionRepository;
import com.jotechhub.subscription.SubscriptionStatus;
import com.jotechhub.user.StudentProfile;
import com.jotechhub.user.StudentProfileRepository;
import com.jotechhub.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.jotechhub.savedevent.SavedEventRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventService {

    private final EventRepository eventRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SavedEventRepository savedEventRepository;
    private final StudentProfileRepository studentProfileRepository;

    @Transactional(readOnly = true)
    public List<AdminEventResponse> getAllEvents(String status) {
        List<Event> events;

        if (status == null || status.isBlank()) {
            events = eventRepository.findAllByOrderByCreatedAtDesc();
        } else {
            EventStatus eventStatus;
            try {
                eventStatus = EventStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid event status");
            }

            events = eventRepository.findByStatusOrderByCreatedAtDesc(eventStatus);
        }

        return events.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminEventResponse getEventById(Long eventId) {
        Event event = eventRepository.findAdminEventById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        return mapToResponse(event);
    }

    public AdminEventResponse approveEvent(Long eventId) {
        Event event = eventRepository.findAdminEventById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (Boolean.TRUE.equals(event.getCancelled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelled event cannot be approved");
        }

        if (event.getStatus() != EventStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending events can be approved");
        }

        event.setStatus(EventStatus.APPROVED);
        event.setRejectionReason(null);
        event.setReviewedAt(LocalDateTime.now());

        event = eventRepository.save(event);

        return mapToResponse(event);
    }

    public AdminEventResponse rejectEvent(Long eventId, RejectEventRequest request) {
        Event event = eventRepository.findAdminEventById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (Boolean.TRUE.equals(event.getCancelled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelled event cannot be rejected");
        }

        if (event.getStatus() != EventStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending events can be rejected");
        }

        event.setStatus(EventStatus.REJECTED);
        event.setRejectionReason(request.getRejectionReason().trim());
        event.setReviewedAt(LocalDateTime.now());

        event = eventRepository.save(event);

        return mapToResponse(event);
    }

    private AdminEventResponse mapToResponse(Event event) {
        return AdminEventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .price(event.getPrice())
                .organizerUserId(event.getOrganizer().getId())
                .organizerName(resolveOrganizerName(event.getOrganizer().getId()))
                .categoryId(event.getCategory().getId())
                .categoryName(event.getCategory().getName())
                .eventDate(event.getEventDate())
                .eventType(event.getEventType().name())
                .cityId(event.getCity().getId())
                .cityName(event.getCity().getName())
                .eventTime(event.getEventTime())
                .eventType(event.getEventType().name())
                .cityId(event.getCity().getId())
                .cityName(event.getCity().getName())
                .location(event.getLocation())
                .registrationLink(event.getRegistrationLink())
                .capacity(event.getCapacity())
                .activeRegistrationsCount(getActiveRegistrationsCount(event.getId()))
                .status(event.getStatus().name())
                .rejectionReason(event.getRejectionReason())
                .reviewedAt(event.getReviewedAt())
                .cancelled(event.getCancelled())
                .cancelledAt(event.getCancelledAt())
                .cancellationReason(event.getCancellationReason())
                .timeState(event.getEventDate().isBefore(LocalDate.now()) ? "PAST" : "UPCOMING")
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .tags(event.getTags().stream().map(tag -> tag.getName()).sorted().toList())
                .build();
    }

    private String resolveOrganizerName(Long userId) {
        return organizerProfileRepository.findByUserId(userId)
                .map(profile -> profile.getOrganizationName())
                .orElse("Organizer");
    }

    private Integer getActiveRegistrationsCount(Long eventId) {
        return (int) subscriptionRepository.countByEventIdAndStatus(
                eventId,
                SubscriptionStatus.ACTIVE
        );
    }
    @Transactional(readOnly = true)
    public List<EventSubscriberResponse> getEventSubscribers(Long eventId) {
        Event event = eventRepository.findAdminEventById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        List<Subscription> subscriptions = subscriptionRepository
                .findByEventIdAndStatus(event.getId(), SubscriptionStatus.ACTIVE);

        return subscriptions.stream()
                .map(this::mapToSubscriberResponse)
                .toList();
    }

    private EventSubscriberResponse mapToSubscriberResponse(Subscription subscription) {
        User student = subscription.getUser();
        StudentProfile profile = studentProfileRepository.findByUserId(student.getId()).orElse(null);

        String fullName = profile != null ? profile.getFullName() : null;
        String university = (profile != null && profile.getUniversity() != null)
                ? profile.getUniversity().getName()
                : null;
        String city = (profile != null && profile.getCity() != null)
                ? profile.getCity().getName()
                : null;

        return EventSubscriberResponse.builder()
                .subscriptionId(subscription.getId())
                .status(subscription.getStatus().name())
                .ticketCode(subscription.getTicketCode())
                .subscribedAt(subscription.getSubscribedAt())
                .userId(student.getId())
                .fullName(fullName)
                .email(student.getEmail())
                .university(university)
                .city(city)
                .profileImageUrl(student.getProfileImageUrl())
                .build();
    }

    public AdminEventResponse deleteEvent(Long eventId, AdminDeleteEventRequest request) {
        Event event = eventRepository.findAdminEventById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (Boolean.TRUE.equals(event.getCancelled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is already deleted");
        }

        event.setCancelled(true);
        event.setCancelledAt(LocalDateTime.now());

        String reason = "Deleted by admin";
        if (request != null
                && request.getCancellationReason() != null
                && !request.getCancellationReason().isBlank()) {
            reason = request.getCancellationReason().trim();
        }

        event.setCancellationReason(reason);

        savedEventRepository.deleteByEventId(event.getId());

        event = eventRepository.save(event);

        return mapToResponse(event);
    }
}