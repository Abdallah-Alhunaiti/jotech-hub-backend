package com.jotechhub.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeactivateUserResponse {

    private Long userId;
    private String email;
    private String role;
    private Boolean active;

    private int cancelledEventsCount;
    private int cancelledSubscriptionsCount;

    private String message;
}