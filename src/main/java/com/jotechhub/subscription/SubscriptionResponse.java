package com.jotechhub.subscription;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {

    private Long subscriptionId;
    private String status;
    private String ticketCode;
    private LocalDateTime subscribedAt;

    private Long eventId;
    private String eventName;
    private String organizerName;
    private String categoryName;

    private LocalDate eventDate;
    private LocalTime eventTime;
    private String eventType;
    private Long cityId;
    private String cityName;
    private String location;
    private BigDecimal price;

    private Boolean cancelled;
    private String timeState;
}