package com.jotechhub.auth;

import com.jotechhub.common.response.ApiSuccessResponse;
import com.jotechhub.organizer.OrganizationType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleOAuth2Service googleOAuth2Service;
    private final Validator validator;

    @PostMapping("/signup/student")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> signupStudent(
            @Valid @RequestBody StudentSignupRequest request
    ) {
        AuthResponse response = authService.signupStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of(response));
    }

    @PostMapping(
            value = "/signup-organization",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> signupOrganizer(
            @Valid @RequestBody OrganizerSignupRequest request
    ) {
        AuthResponse response = authService.signupOrganizer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of(response));
    }

    @PostMapping(
            value = "/signup-organization",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> signupOrganizerWithImage(
            @RequestParam("organizationName") String organizationName,
            @RequestParam("email") String email,
            @RequestParam("universityId") Long universityId,
            @RequestParam("cityId") Long cityId,
            @RequestParam("organizationType") String organizationType,
            @RequestParam("shortDescription") String shortDescription,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("termsAccepted") Boolean termsAccepted,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        OrganizerSignupRequest request = new OrganizerSignupRequest();

        request.setOrganizationName(organizationName);
        request.setEmail(email);
        request.setUniversityId(universityId);
        request.setCityId(cityId);
        request.setOrganizationType(parseOrganizationType(organizationType));
        request.setShortDescription(shortDescription);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        request.setTermsAccepted(termsAccepted);

        validateOrganizerSignupRequest(request);

        AuthResponse response = authService.signupOrganizer(request, image);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiSuccessResponse.of(response));
    }

    @PostMapping("/oauth2/google/complete")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> completeGoogleSignup(
            @Valid @RequestBody GoogleCompleteSignupRequest request
    ) {
        AuthResponse response = googleOAuth2Service.completeGoogleSignup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of(response));
    }

    @PostMapping("/forgot-password")
    public ApiSuccessResponse<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ApiSuccessResponse.of(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ApiSuccessResponse<ResetPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ApiSuccessResponse.of(authService.resetPassword(request));
    }

    private OrganizationType parseOrganizationType(String organizationType) {
        if (organizationType == null || organizationType.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Organization type is required"
            );
        }

        try {
            return OrganizationType.valueOf(organizationType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid organization type"
            );
        }
    }

    private void validateOrganizerSignupRequest(OrganizerSignupRequest request) {
        Set<ConstraintViolation<OrganizerSignupRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}