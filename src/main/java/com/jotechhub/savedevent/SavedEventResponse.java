package com.jotechhub.savedevent;

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
public class SavedEventResponse {

    private Long savedEventId;
    private LocalDateTime savedAt;

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

    private String eventStatus;
    private Boolean cancelled;
    private String timeState;
}