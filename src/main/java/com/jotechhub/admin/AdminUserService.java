package com.jotechhub.admin;

import com.jotechhub.event.Event;
import com.jotechhub.event.EventRepository;
import com.jotechhub.event.EventStatus;
import com.jotechhub.organizer.OrganizerProfile;
import com.jotechhub.organizer.OrganizerProfileRepository;
import com.jotechhub.role.RoleType;
import com.jotechhub.subscription.Subscription;
import com.jotechhub.subscription.SubscriptionRepository;
import com.jotechhub.subscription.SubscriptionStatus;
import com.jotechhub.user.StudentProfile;
import com.jotechhub.user.StudentProfileRepository;
import com.jotechhub.user.User;
import com.jotechhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers(String role, Boolean active) {
        List<User> users;

        if (role != null && !role.isBlank() && active != null) {
            RoleType roleType = parseRole(role);
            users = userRepository.findByRoleAndActiveOrderByCreatedAtDesc(roleType, active);
        } else if (role != null && !role.isBlank()) {
            RoleType roleType = parseRole(role);
            users = userRepository.findByRoleOrderByCreatedAtDesc(roleType);
        } else if (active != null) {
            users = userRepository.findByActiveOrderByCreatedAtDesc(active);
        } else {
            users = userRepository.findAllByOrderByCreatedAtDesc();
        }

        return users.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return mapToResponse(user);
    }

    public DeactivateUserResponse deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == RoleType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin accounts cannot be deactivated from this endpoint");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already deactivated");
        }

        int cancelledEventsCount = 0;
        int cancelledSubscriptionsCount = 0;

        if (user.getRole() == RoleType.STUDENT) {
            cancelledSubscriptionsCount = cancelFutureStudentSubscriptions(user);
        } else if (user.getRole() == RoleType.ORGANIZER) {
            DeactivationSummary summary = deactivateOrganizerAndRelatedData(user);
            cancelledEventsCount = summary.cancelledEventsCount();
            cancelledSubscriptionsCount = summary.cancelledSubscriptionsCount();
        }

        user.setActive(false);
        userRepository.save(user);

        return DeactivateUserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.getActive())
                .cancelledEventsCount(cancelledEventsCount)
                .cancelledSubscriptionsCount(cancelledSubscriptionsCount)
                .message("User deactivated successfully")
                .build();
    }

    private int cancelFutureStudentSubscriptions(User student) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndStatus(
                student.getId(),
                SubscriptionStatus.ACTIVE
        );

        int cancelledCount = 0;

        for (Subscription subscription : subscriptions) {
            if (!isEventPast(subscription.getEvent())) {
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                cancelledCount++;
            }
        }

        subscriptionRepository.saveAll(subscriptions);
        return cancelledCount;
    }

    private DeactivationSummary deactivateOrganizerAndRelatedData(User organizer) {
        List<Event> events = eventRepository.findByOrganizerId(organizer.getId());

        List<Event> eventsToUpdate = new ArrayList<>();
        List<Subscription> subscriptionsToUpdate = new ArrayList<>();

        int cancelledEventsCount = 0;
        int cancelledSubscriptionsCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Event event : events) {
            boolean shouldCancel = false;

            if (Boolean.TRUE.equals(event.getCancelled())) {
                continue;
            }

            if (event.getStatus() == EventStatus.PENDING) {
                shouldCancel = true;
            } else if (event.getStatus() == EventStatus.APPROVED && !isEventPast(event)) {
                shouldCancel = true;
            }

            if (shouldCancel) {
                event.setCancelled(true);
                event.setCancelledAt(now);
                event.setCancellationReason("Organizer account deactivated by admin");
                eventsToUpdate.add(event);
                cancelledEventsCount++;

                List<Subscription> activeSubscriptions = subscriptionRepository.findByEventIdAndStatus(
                        event.getId(),
                        SubscriptionStatus.ACTIVE
                );

                for (Subscription subscription : activeSubscriptions) {
                    subscription.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionsToUpdate.add(subscription);
                    cancelledSubscriptionsCount++;
                }
            }
        }

        if (!eventsToUpdate.isEmpty()) {
            eventRepository.saveAll(eventsToUpdate);
        }

        if (!subscriptionsToUpdate.isEmpty()) {
            subscriptionRepository.saveAll(subscriptionsToUpdate);
        }

        return new DeactivationSummary(cancelledEventsCount, cancelledSubscriptionsCount);
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

    private RoleType parseRole(String role) {
        try {
            return RoleType.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }
    }

    private AdminUserResponse mapToResponse(User user) {
        StudentProfile studentProfile = null;
        OrganizerProfile organizerProfile = null;

        if (user.getRole() == RoleType.STUDENT) {
            studentProfile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        } else if (user.getRole() == RoleType.ORGANIZER) {
            organizerProfile = organizerProfileRepository.findByUserId(user.getId()).orElse(null);
        }

        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .displayName(resolveDisplayName(user, studentProfile, organizerProfile))
                .universityName(resolveUniversityName(studentProfile, organizerProfile))
                .cityName(resolveCityName(studentProfile, organizerProfile))
                .organizationType(organizerProfile != null && organizerProfile.getOrganizationType() != null
                        ? organizerProfile.getOrganizationType().name()
                        : null)
                .build();
    }

    private String resolveDisplayName(User user, StudentProfile studentProfile, OrganizerProfile organizerProfile) {
        return switch (user.getRole()) {
            case STUDENT -> studentProfile != null ? studentProfile.getFullName() : user.getEmail();
            case ORGANIZER -> organizerProfile != null ? organizerProfile.getOrganizationName() : user.getEmail();
            case ADMIN -> "Admin";
        };
    }

    private String resolveUniversityName(StudentProfile studentProfile, OrganizerProfile organizerProfile) {
        if (studentProfile != null && studentProfile.getUniversity() != null) {
            return studentProfile.getUniversity().getName();
        }

        if (organizerProfile != null && organizerProfile.getUniversity() != null) {
            return organizerProfile.getUniversity().getName();
        }

        return null;
    }

    private String resolveCityName(StudentProfile studentProfile, OrganizerProfile organizerProfile) {
        if (studentProfile != null && studentProfile.getCity() != null) {
            return studentProfile.getCity().getName();
        }

        if (organizerProfile != null && organizerProfile.getCity() != null) {
            return organizerProfile.getCity().getName();
        }

        return null;
    }

    private record DeactivationSummary(int cancelledEventsCount, int cancelledSubscriptionsCount) {
    }
}