package com.jotechhub.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicEventDetailsResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String organizerName;
    private Long categoryId;
    private String categoryName;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String location;
    private String registrationLink;
    private Integer capacity;
    private List<String> tags;
    private String timeState;
}