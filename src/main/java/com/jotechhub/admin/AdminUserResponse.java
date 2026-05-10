package com.jotechhub.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {

    private Long id;
    private String email;
    private String role;
    private Boolean active;
    private LocalDateTime createdAt;

    private String displayName;

    private String universityName;
    private String cityName;

    private String organizationType;
}