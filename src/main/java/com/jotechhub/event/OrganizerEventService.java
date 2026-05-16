package com.jotechhub.event;

import com.jotechhub.category.Category;
import com.jotechhub.category.CategoryRepository;
import com.jotechhub.tag.Tag;
import com.jotechhub.tag.TagRepository;
import com.jotechhub.user.StudentProfile;
import com.jotechhub.user.StudentProfileRepository;
import com.jotechhub.user.User;
import com.jotechhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.jotechhub.subscription.EventSubscriberResponse;
import com.jotechhub.subscription.Subscription;
import com.jotechhub.subscription.SubscriptionRepository;
import com.jotechhub.subscription.SubscriptionStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import com.jotechhub.city.City;
import com.jotechhub.city.CityRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizerEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StudentProfileRepository studentProfileRepository;
    public OrganizerEventResponse createEvent(CreateEventRequest request, Authentication authentication) {
        User organizer = getCurrentOrganizer(authentication);
        Category category = getCategoryOrThrow(request.getCategoryId());
        Set<Tag> tags = getTags(request.getTagIds());
        City city = getCityOrThrow(request.getCityId());

        Event event = Event.builder()
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .price(request.getPrice())
                .organizer(organizer)
                .category(category)
                .city(city)
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .eventType(request.getEventType())
                .location(request.getLocation().trim())
                .registrationLink(normalizeNullable(request.getRegistrationLink()))
                .capacity(request.getCapacity())
                .status(EventStatus.PENDING)
                .cancelled(false)
                .tags(tags)
                .build();

        event = eventRepository.save(event);
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public List<OrganizerEventResponse> getMyEvents(Authentication authentication) {
        User organizer = getCurrentOrganizer(authentication);

        return eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizerEventResponse getMyEventById(Long eventId, Authentication authentication) {
        User organizer = getCurrentOrganizer(authentication);

        Event event = eventRepository.findByIdAndOrganizerId(eventId, organizer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        return mapToResponse(event);
    }

    public OrganizerEventResponse updateEvent(Long eventId, UpdateEventRequest request, Authentication authentication) {
        User organizer = getCurrentOrganizer(authentication);

        Event event = eventRepository.findByIdAndOrganizerId(eventId, organizer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (Boolean.TRUE.equals(event.getCancelled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelled event cannot be updated");
        }

        if (event.getEventDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past events cannot be updated");
        }

        boolean importantFieldsChanged = hasImportantFieldChanges(event, request);

        Category category = getCategoryOrThrow(request.getCategoryId());
        City city = getCityOrThrow(request.getCityId());
        Set<Tag> tags = getTags(request.getTagIds());

        event.setName(request.getName().trim());
        event.setDescription(request.getDescription().trim());
        event.setPrice(request.getPrice());
        event.setCategory(category);
        event.setCity(city);
        event.setEventDate(request.getEventDate());
        event.setEventTime(request.getEventTime());
        event.setEventType(request.getEventType());
        event.setLocation(request.getLocation().trim());
        event.setRegistrationLink(normalizeNullable(request.getRegistrationLink()));
        event.setCapacity(request.getCapacity());
        event.setTags(tags);

        if (event.getStatus() == EventStatus.REJECTED) {
            event.setStatus(EventStatus.PENDING);
            event.setRejectionReason(null);
            event.setReviewedAt(null);
        } else if (event.getStatus() == EventStatus.APPROVED && importantFieldsChanged) {
            event.setStatus(EventStatus.PENDING);
            event.setRejectionReason(null);
            event.setReviewedAt(null);
        }

        event = eventRepository.save(event);
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventSubscriberResponse> getEventSubscribers(Long eventId, Authentication authentication) {
        User organizer = getCurrentOrganizer(authentication);

        Event event = eventRepository.findByIdAndOrganizerId(eventId, organizer.getId())
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

    public OrganizerEventResponse deleteEvent(Long eventId, Authentication authentication) {
        User organizer = getCurrentOrganizer(authentication);

        Event event = eventRepository.findByIdAndOrganizerId(eventId, organizer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (Boolean.TRUE.equals(event.getCancelled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is already cancelled");
        }

        if (event.getEventDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past events cannot be deleted");
        }

        event.setCancelled(true);
        event.setCancelledAt(LocalDateTime.now());
        event.setCancellationReason("Cancelled by organizer");

        event = eventRepository.save(event);
        return mapToResponse(event);
    }

    private User getCurrentOrganizer(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is deactivated");
        }

        if (user.getRole() != com.jotechhub.role.RoleType.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only organizers can manage their own events");
        }

        return user;
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
    }

    private City getCityOrThrow(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "City not found"));
    }
    private Set<Tag> getTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Long> uniqueIds = new HashSet<>(tagIds);
        List<Tag> tags = tagRepository.findAllById(uniqueIds);

        if (tags.size() != uniqueIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more tags were not found");
        }

        return new HashSet<>(tags);
    }

    private boolean hasImportantFieldChanges(Event event, UpdateEventRequest request) {
        return !Objects.equals(event.getName(), request.getName().trim())
                || !Objects.equals(event.getDescription(), request.getDescription().trim())
                || !Objects.equals(event.getCategory().getId(), request.getCategoryId())
                || !Objects.equals(event.getCity().getId(), request.getCityId())
                || !Objects.equals(event.getEventDate(), request.getEventDate())
                || !Objects.equals(event.getEventTime(), request.getEventTime())
                || !Objects.equals(event.getEventType(), request.getEventType())
                || !Objects.equals(event.getLocation(), request.getLocation().trim())
                || !Objects.equals(normalizeNullable(event.getRegistrationLink()), normalizeNullable(request.getRegistrationLink()))
                || !Objects.equals(event.getCapacity(), request.getCapacity());
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private OrganizerEventResponse mapToResponse(Event event) {
        return OrganizerEventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .price(event.getPrice())
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
                .tags(event.getTags().stream().map(Tag::getName).sorted().toList())
                .build();
    }
    private Integer getActiveRegistrationsCount(Long eventId) {
        return (int) subscriptionRepository.countByEventIdAndStatus(
                eventId,
                SubscriptionStatus.ACTIVE
        );
    }
}