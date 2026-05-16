package com.jotechhub.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicEventCardResponse {

    private Long id;
    private String name;
    private String organizerName;
    private String categoryName;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String eventType;
    private Long cityId;
    private String cityName;
    private String location;
    private BigDecimal price;
    private Integer capacity;
    private Integer activeRegistrationsCount;
    private String timeState;
}