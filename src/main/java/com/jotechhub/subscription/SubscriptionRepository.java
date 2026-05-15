package com.jotechhub.subscription;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @EntityGraph(attributePaths = {"event", "event.category", "event.organizer"})
    List<Subscription> findByUserIdOrderBySubscribedAtDesc(Long userId);

    List<Subscription> findByEventId(Long eventId);

    Optional<Subscription> findByTicketCode(String ticketCode);

    Optional<Subscription> findByUserIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, SubscriptionStatus status);

    boolean existsByTicketCode(String ticketCode);

    @EntityGraph(attributePaths = {"event"})
    List<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    @EntityGraph(attributePaths = {"event", "user"})
    List<Subscription> findByEventIdAndStatus(Long eventId, SubscriptionStatus status);
}