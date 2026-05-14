package com.jotechhub.auth;

import lombok.Builder;

@Builder
public record GoogleLoginSuccessData(
        boolean requiresOnboarding,
        String pendingToken,

        Long userId,
        String email,
        String role,
        String displayName,
        String accessToken,
        String tokenType,
        Long expiresIn
) {
}