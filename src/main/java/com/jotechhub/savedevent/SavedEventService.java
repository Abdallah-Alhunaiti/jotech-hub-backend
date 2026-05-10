package com.jotechhub.savedevent;

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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedEventService {

    private final SavedEventRepository savedEventRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizerProfileRepository organizerProfileRepository;

    public SavedEventResponse saveEvent(Long eventId, Authentication authentication) {
        User student = getCurrentStudent(authentication);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (event.getStatus() != EventStatus.APPROVED || Boolean.TRUE.equals(event.getCancelled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved and active events can be saved");
        }

        if (savedEventRepository.existsByUserIdAndEventId(student.getId(), eventId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is already saved");
        }

        SavedEvent savedEvent = SavedEvent.builder()
                .user(student)
                .event(event)
                .build();

        savedEvent = savedEventRepository.save(savedEvent);
        return mapToResponse(savedEvent);
    }

    public void unsaveEvent(Long eventId, Authentication authentication) {
        User student = getCurrentStudent(authentication);

        SavedEvent savedEvent = savedEventRepository.findByUserIdAndEventId(student.getId(), eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved event not found"));

        savedEventRepository.delete(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<SavedEventResponse> getMySavedEvents(Authentication authentication) {
        User student = getCurrentStudent(authentication);

        return savedEventRepository.findByUserIdOrderBySavedAtDesc(student.getId())
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can manage saved events");
        }

        return user;
    }

    private SavedEventResponse mapToResponse(SavedEvent savedEvent) {
        Event event = savedEvent.getEvent();

        return SavedEventResponse.builder()
                .savedEventId(savedEvent.getId())
                .savedAt(savedEvent.getSavedAt())
                .eventId(event.getId())
                .eventName(event.getName())
                .organizerName(resolveOrganizerName(event.getOrganizer().getId()))
                .categoryName(event.getCategory().getName())
                .eventDate(event.getEventDate())
                .eventTime(event.getEventTime())
                .location(event.getLocation())
                .price(event.getPrice())
                .eventStatus(event.getStatus().name())
                .cancelled(event.getCancelled())
                .timeState(event.getEventDate().isBefore(LocalDate.now()) ? "PAST" : "UPCOMING")
                .build();
    }

    private String resolveOrganizerName(Long userId) {
        return organizerProfileRepository.findByUserId(userId)
                .map(profile -> profile.getOrganizationName())
                .orElse("Organizer");
    }
}