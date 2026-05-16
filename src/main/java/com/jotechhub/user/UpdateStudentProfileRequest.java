package com.jotechhub.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "University is required")
    private Long universityId;

    @NotNull(message = "City is required")
    private Long cityId;
}