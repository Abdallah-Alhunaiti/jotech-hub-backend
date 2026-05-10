package com.jotechhub.auth;

import com.jotechhub.common.response.ApiSuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/student")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> signupStudent(@Valid @RequestBody StudentSignupRequest request) {
        AuthResponse response = authService.signupStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of(response));
    }

    @PostMapping("/signup-organization")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> signupOrganizer(@Valid @RequestBody OrganizerSignupRequest request) {
        AuthResponse response = authService.signupOrganizer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiSuccessResponse.of(response));
    }
}