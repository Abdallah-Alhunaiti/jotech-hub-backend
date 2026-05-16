package com.jotechhub.organizer;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerDashboardEventItemResponse {

    private Long id;
    private String name;
    private String categoryName;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String eventType;
    private Long cityId;
    private String cityName;
    private String location;

    private String status;
    private Boolean cancelled;
    private String timeState;

    private long activeRegistrationsCount;
    private LocalDateTime createdAt;
}