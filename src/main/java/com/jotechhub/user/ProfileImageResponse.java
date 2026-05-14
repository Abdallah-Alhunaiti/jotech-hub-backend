package com.jotechhub.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileImageResponse {

    private Long userId;
    private String profileImageUrl;
    private String message;
}