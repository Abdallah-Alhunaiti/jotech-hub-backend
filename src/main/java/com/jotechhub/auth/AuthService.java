package com.jotechhub.auth;

import com.jotechhub.city.City;
import com.jotechhub.city.CityRepository;
import com.jotechhub.organizer.OrganizerProfile;
import com.jotechhub.organizer.OrganizerProfileRepository;
import com.jotechhub.role.RoleType;
import com.jotechhub.university.University;
import com.jotechhub.university.UniversityRepository;
import com.jotechhub.user.StudentProfile;
import com.jotechhub.user.StudentProfileRepository;
import com.jotechhub.user.User;
import com.jotechhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import com.jotechhub.security.JwtService;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final UniversityRepository universityRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse signupStudent(StudentSignupRequest request) {
        validateEmailNotUsed(request.getEmail());
        validatePasswords(request.getPassword(), request.getConfirmPassword());

        University university = getUniversityOrThrow(request.getUniversityId());
        City city = getCityOrThrow(request.getCityId());

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleType.STUDENT)
                .active(true)
                .emailVerified(false)
                .termsAccepted(Boolean.TRUE.equals(request.getTermsAccepted()))
                .termsAcceptedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        AuthAccount authAccount = AuthAccount.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .providerUserId(user.getEmail())
                .providerEmail(user.getEmail())
                .build();

        authAccountRepository.save(authAccount);

        StudentProfile studentProfile = StudentProfile.builder()
                .user(user)
                .fullName(request.getFullName().trim())
                .university(university)
                .city(city)
                .build();

        studentProfileRepository.save(studentProfile);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .displayName(studentProfile.getFullName())
                .message("Student account created successfully")
                .build();
    }

    public AuthResponse signupOrganizer(OrganizerSignupRequest request) {
        System.out.println("signupOrganizer service reached");
        validateEmailNotUsed(request.getEmail());
        validatePasswords(request.getPassword(), request.getConfirmPassword());

        University university = getUniversityOrThrow(request.getUniversityId());
        City city = getCityOrThrow(request.getCityId());

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleType.ORGANIZER)
                .active(true)
                .emailVerified(false)
                .termsAccepted(Boolean.TRUE.equals(request.getTermsAccepted()))
                .termsAcceptedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        AuthAccount authAccount = AuthAccount.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .providerUserId(user.getEmail())
                .providerEmail(user.getEmail())
                .build();

        authAccountRepository.save(authAccount);

        OrganizerProfile organizerProfile = OrganizerProfile.builder()
                .user(user)
                .organizationName(request.getOrganizationName().trim())
                .university(university)
                .city(city)
                .organizationType(request.getOrganizationType())
                .shortDescription(request.getShortDescription().trim())
                .build();

        organizerProfileRepository.save(organizerProfile);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .displayName(organizerProfile.getOrganizationName())
                .message("Organizer account created successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email or password"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is deactivated");
        }

        authAccountRepository.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "This account does not support email/password login"));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .displayName(resolveDisplayName(user))
                .message("Login successful")
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationInSeconds())
                .build();
    }

    private void validateEmailNotUsed(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already registered");
        }
    }

    private void validatePasswords(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password and confirm password do not match");
        }
    }

    private University getUniversityOrThrow(Long universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected university was not found"));
    }

    private City getCityOrThrow(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected city was not found"));
    }

    private String resolveDisplayName(User user) {
        return switch (user.getRole()) {
            case STUDENT -> studentProfileRepository.findByUserId(user.getId())
                    .map(StudentProfile::getFullName)
                    .orElse(user.getEmail());

            case ORGANIZER -> organizerProfileRepository.findByUserId(user.getId())
                    .map(OrganizerProfile::getOrganizationName)
                    .orElse(user.getEmail());

            case ADMIN -> "Admin";
        };
    }
}