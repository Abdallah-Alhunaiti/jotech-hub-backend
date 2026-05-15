package com.jotechhub.subscription;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSubscriberResponse {

    private Long subscriptionId;
    private String status;
    private String ticketCode;
    private LocalDateTime subscribedAt;

    private Long userId;
    private String fullName;
    private String email;
    private String university;
    private String city;
    private String profileImageUrl;
}