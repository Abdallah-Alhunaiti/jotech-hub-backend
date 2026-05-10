package com.jotechhub.savedevent;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedEventRepository extends JpaRepository<SavedEvent, Long> {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    void deleteByUserIdAndEventId(Long userId, Long eventId);

    @EntityGraph(attributePaths = {"event", "event.category"})
    List<SavedEvent> findByUserIdOrderBySavedAtDesc(Long userId);

    Optional<SavedEvent> findByUserIdAndEventId(Long userId, Long eventId);
}