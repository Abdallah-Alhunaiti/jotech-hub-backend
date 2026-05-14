package com.jotechhub.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordResponse {

    private String message;

    private String resetToken;
    private String resetLink;
}