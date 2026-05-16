package com.jotechhub.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileResponse {

    private Long userId;
    private String email;

    private String fullName;
    private String gender;

    private Long universityId;
    private String universityName;

    private Long cityId;
    private String cityName;
}