package com.jotechhub.auth;

import com.jotechhub.user.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

// DTO used for student signup requests
@Getter
@Setter
public class StudentSignupRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "University is required")
    private Long universityId;

    @NotNull(message = "City is required")
    private Long cityId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotNull(message = "Terms acceptance is required")
    @AssertTrue(message = "You must accept terms and conditions")
    private Boolean termsAccepted;
}