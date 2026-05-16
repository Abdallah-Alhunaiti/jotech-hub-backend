package com.jotechhub.subscription;

import com.jotechhub.event.Event;
import com.jotechhub.event.EventRepository;
import com.jotechhub.event.EventStatus;
import com.jotechhub.organizer.OrganizerProfileRepository;
import com.jotechhub.role.RoleType;
import com.jotechhub.user.User;
import com.jotechhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizerProfileRepository organizerProfileRepository;

    public SubscriptionResponse subscribeToEvent(Long eventId, Authentication authentication) {
        User student = getCurrentStudent(authentication);

        Event event = eventRepository.findByIdForSubscription(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        validateEventCanBeSubscribed(event);

        Subscription existingSubscription = subscriptionRepository.findByUserIdAndEventId(student.getId(), eventId)
                .orElse(null);

        if (existingSubscription != null && existingSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already subscribed to this event");
        }

        long activeSubscriptions = subscriptionRepository.countByEventIdAndStatus(eventId, SubscriptionStatus.ACTIVE);
        if (activeSubscriptions >= event.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event capacity has been reached");
        }

        Subscription subscription;
        if (existingSubscription != null && existingSubscription.getStatus() == SubscriptionStatus.CANCELLED) {
            existingSubscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription = subscriptionRepository.save(existingSubscription);
        } else {
            subscription = Subscription.builder()
                    .user(student)
                    .event(event)
                    .ticketCode(generateUniqueTicketCode())
                    .status(SubscriptionStatus.ACTIVE)
                    .build();

            subscription = subscriptionRepository.save(subscription);
        }

        return mapToResponse(subscription);
    }

    public SubscriptionResponse cancelSubscription(Long eventId, Authentication authentication) {
        User student = getCurrentStudent(authentication);

        Subscription subscription = subscriptionRepository.findByUserIdAndEventId(student.getId(), eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription is already cancelled");
        }

        if (isEventPast(subscription.getEvent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past event subscription cannot be cancelled");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription = subscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(Authentication authentication) {
        User student = getCurrentStudent(authentication);

        return subscriptionRepository.findByUserIdOrderBySubscribedAtDesc(student.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getCurrentStudent(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is deactivated");
        }

        if (user.getRole() != RoleType.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can manage subscriptions");
        }

        return user;
    }

    private void validateEventCanBeSubscribed(Event event) {
        if (event.getStatus() != EventStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved events can be subscribed to");
        }

        if (Boolean.TRUE.equals(event.getCancelled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelled events cannot be subscribed to");
        }

        if (isEventPast(event)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past events cannot be subscribed to");
        }
    }

    private boolean isEventPast(Event event) {
        LocalDate today = LocalDate.now();

        if (event.getEventDate().isBefore(today)) {
            return true;
        }

        if (event.getEventDate().isEqual(today) && event.getEventTime() != null) {
            return event.getEventTime().isBefore(LocalDateTime.now().toLocalTime());
        }

        return false;
    }

    private String generateUniqueTicketCode() {
        String code;
        do {
            code = "JTH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (subscriptionRepository.existsByTicketCode(code));

        return code;
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        Event event = subscription.getEvent();

        return SubscriptionResponse.builder()
                .subscriptionId(subscription.getId())
                .status(subscription.getStatus().name())
                .ticketCode(subscription.getTicketCode())
                .subscribedAt(subscription.getSubscribedAt())
                .eventId(event.getId())
                .eventName(event.getName())
                .organizerName(resolveOrganizerName(event.getOrganizer().getId()))
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
                .price(event.getPrice())
                .cancelled(event.getCancelled())
                .timeState(isEventPast(event) ? "PAST" : "UPCOMING")
                .build();
    }

    private String resolveOrganizerName(Long userId) {
        return organizerProfileRepository.findByUserId(userId)
                .map(profile -> profile.getOrganizationName())
                .orElse("Organizer");
    }
}