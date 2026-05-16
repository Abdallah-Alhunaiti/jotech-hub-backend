package com.jotechhub.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long userId;
    private String email;
    private String role;
    private String displayName;
    private String organizationImageUrl;
    private String message;

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
}