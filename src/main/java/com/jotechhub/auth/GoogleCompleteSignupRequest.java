package com.jotechhub.auth;

import com.jotechhub.organizer.OrganizationType;
import com.jotechhub.user.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleCompleteSignupRequest {

    @NotBlank(message = "Pending token is required")
    private String pendingToken;

    @NotBlank(message = "Account type is required")
    private String accountType;

    @NotNull(message = "Gender is required")
    private Gender gender;

    // Student fields
    private String fullName;

    private Long universityId;
    private Long cityId;

    // Organizer fields
    private String organizationName;
    private OrganizationType organizationType;
    private String shortDescription;

    @NotNull(message = "Terms acceptance is required")
    @AssertTrue(message = "You must accept terms and conditions")
    private Boolean termsAccepted;
}