package com.jotechhub.event;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizerId(Long organizerId);

    List<Event> findByOrganizerIdAndCancelledFalse(Long organizerId);

    Page<Event> findByStatusAndCancelledFalse(EventStatus status, Pageable pageable);

    long countByOrganizerId(Long organizerId);

    long countByOrganizerIdAndEventDateGreaterThanEqualAndCancelledFalse(Long organizerId, LocalDate date);

    long countByOrganizerIdAndEventDateLessThanAndCancelledFalse(Long organizerId, LocalDate date);

    long countByOrganizerIdAndCancelledFalse(Long organizerId);

    @Query(value = """
        SELECT DISTINCT e
        FROM Event e
        LEFT JOIN e.tags t
        WHERE e.status = com.jotechhub.event.EventStatus.APPROVED
          AND e.cancelled = false
          AND (
                :keyword = ''
                OR LOWER(e.name) LIKE CONCAT('%', LOWER(:keyword), '%')
                OR LOWER(e.description) LIKE CONCAT('%', LOWER(:keyword), '%')
              )
          AND (:categoryId IS NULL OR e.category.id = :categoryId)
          AND (:cityId IS NULL OR e.city.id = :cityId)
          AND (:tagId IS NULL OR t.id = :tagId)
          AND (
                :location = ''
                OR LOWER(e.location) LIKE CONCAT('%', LOWER(:location), '%')
              )
          AND (:dateFrom IS NULL OR e.eventDate >= :dateFrom)
          AND (:dateTo IS NULL OR e.eventDate <= :dateTo)
        """,
            countQuery = """
                SELECT COUNT(DISTINCT e.id)
                FROM Event e
                LEFT JOIN e.tags t
                WHERE e.status = com.jotechhub.event.EventStatus.APPROVED
                  AND e.cancelled = false
                  AND (
                        :keyword = ''
                        OR LOWER(e.name) LIKE CONCAT('%', LOWER(:keyword), '%')
                        OR LOWER(e.description) LIKE CONCAT('%', LOWER(:keyword), '%')
                      )
                  AND (:categoryId IS NULL OR e.category.id = :categoryId)
                  AND (:cityId IS NULL OR e.city.id = :cityId)
                  AND (:tagId IS NULL OR t.id = :tagId)
                  AND (
                        :location = ''
                        OR LOWER(e.location) LIKE CONCAT('%', LOWER(:location), '%')
                      )
                  AND (:dateFrom IS NULL OR e.eventDate >= :dateFrom)
                  AND (:dateTo IS NULL OR e.eventDate <= :dateTo)
                """)
    Page<Event> searchPublicEvents(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("cityId") Long cityId,
            @Param("tagId") Long tagId,
            @Param("location") String location,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "tags", "organizer"})
    Optional<Event> findByIdAndStatusAndCancelledFalse(Long id, EventStatus status);

    @EntityGraph(attributePaths = {"category", "organizer"})
    List<Event> findTop6ByStatusAndCancelledFalseAndEventDateGreaterThanEqualOrderByEventDateAscEventTimeAsc(
            EventStatus status,
            LocalDate date
    );

    @EntityGraph(attributePaths = {"category", "organizer"})
    List<Event> findTop6ByStatusAndCancelledFalseOrderByCreatedAtDesc(EventStatus status);

    @EntityGraph(attributePaths = {"category", "tags"})
    List<Event> findByOrganizerIdOrderByCreatedAtDesc(Long organizerId);

    @EntityGraph(attributePaths = {"category", "tags"})
    Optional<Event> findByIdAndOrganizerId(Long id, Long organizerId);

    @EntityGraph(attributePaths = {"category", "tags", "organizer"})
    List<Event> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"category", "tags", "organizer"})
    List<Event> findByStatusOrderByCreatedAtDesc(EventStatus status);

    @EntityGraph(attributePaths = {"category", "tags", "organizer"})
    @Query("SELECT e FROM Event e WHERE e.id = :eventId")
    Optional<Event> findAdminEventById(@Param("eventId") Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :eventId")
    Optional<Event> findByIdForSubscription(@Param("eventId") Long eventId);
}
