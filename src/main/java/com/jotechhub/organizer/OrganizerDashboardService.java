package com.jotechhub.organizer;

import com.jotechhub.event.Event;
import com.jotechhub.event.EventRepository;
import com.jotechhub.role.RoleType;
import com.jotechhub.subscription.SubscriptionRepository;
import com.jotechhub.subscription.SubscriptionStatus;
import com.jotechhub.user.User;
import com.jotechhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerDashboardService {

    private final UserRepository userRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;

    public OrganizerDashboardResponse getDashboard(Authentication authentication) {
        User organizer = getCurrentOrganizer(authentication);

        String organizerName = organizerProfileRepository.findByUserId(organizer.getId())
                .map(OrganizerProfile::getOrganizationName)
                .orElse(organizer.getEmail());

        OrganizerDashboardStatsResponse stats = OrganizerDashboardStatsResponse.builder()
                .totalEvents(eventRepository.countByOrganizerIdAndCancelledFalse(organizer.getId()))
                .upcomingEvents(eventRepository.countByOrganizerIdAndEventDateGreaterThanEqualAndCancelledFalse(
                        organizer.getId(),
                        LocalDate.now()
                ))
                .pastEvents(eventRepository.countByOrganizerIdAndEventDateLessThanAndCancelledFalse(
                        organizer.getId(),
                        LocalDate.now()
                ))
                .build();

        List<OrganizerDashboardEventItemResponse> recentEvents = eventRepository
                .findByOrganizerIdOrderByCreatedAtDesc(organizer.getId())
                .stream()
                .limit(10)
                .map(this::mapToEventItemResponse)
                .toList();

        return OrganizerDashboardResponse.builder()
                .organizerUserId(organizer.getId())
                .organizerName(organizerName)
                .stats(stats)
                .recentEvents(recentEvents)
                .build();
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

        if (user.getRole() != RoleType.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only organizers can access the dashboard");
        }

        return user;
    }

    private OrganizerDashboardEventItemResponse mapToEventItemResponse(Event event) {
        return OrganizerDashboardEventItemResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .categoryName(event.getCategory().getName())
                .eventDate(event.getEventDate())
                .eventTime(event.getEventTime())
                .location(event.getLocation())
                .status(event.getStatus().name())
                .cancelled(event.getCancelled())
                .timeState(event.getEventDate().isBefore(LocalDate.now()) ? "PAST" : "UPCOMING")
                .activeRegistrationsCount(
                        subscriptionRepository.countByEventIdAndStatus(event.getId(), SubscriptionStatus.ACTIVE)
                )
                .createdAt(event.getCreatedAt())
                .build();
    }
}