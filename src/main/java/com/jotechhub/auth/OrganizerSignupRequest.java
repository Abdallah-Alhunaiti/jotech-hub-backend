package com.jotechhub.auth;

import com.jotechhub.organizer.OrganizationType;
import com.jotechhub.user.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizerSignupRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 150, message = "Organization name must not exceed 150 characters")
    private String organizationName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotNull(message = "University is required")
    private Long universityId;

    @NotNull(message = "City is required")
    private Long cityId;

    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;

    @NotBlank(message = "Short description is required")
    @Size(max = 1000, message = "Short description must not exceed 1000 characters")
    private String shortDescription;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotNull(message = "Terms acceptance is required")
    @AssertTrue(message = "You must accept terms and conditions")
    private Boolean termsAccepted;
}