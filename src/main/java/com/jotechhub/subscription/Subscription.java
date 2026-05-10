package com.jotechhub.subscription;

import com.jotechhub.event.Event;
import com.jotechhub.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "event_id"}),
                @UniqueConstraint(columnNames = {"ticket_code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 100)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime subscribedAt;
}