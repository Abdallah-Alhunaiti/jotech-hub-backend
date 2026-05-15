package com.jotechhub.auth;

import com.jotechhub.city.City;
import com.jotechhub.city.CityRepository;
import com.jotechhub.organizer.OrganizerProfile;
import com.jotechhub.organizer.OrganizerProfileRepository;
import com.jotechhub.role.RoleType;
import com.jotechhub.security.JwtService;
import com.jotechhub.university.University;
import com.jotechhub.university.UniversityRepository;
import com.jotechhub.user.StudentProfile;
import com.jotechhub.user.StudentProfileRepository;
import com.jotechhub.user.User;
import com.jotechhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleOAuth2Service {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final PendingGoogleSignupRepository pendingGoogleSignupRepository;

    private final StudentProfileRepository studentProfileRepository;
    private final OrganizerProfileRepository organizerProfileRepository;

    private final UniversityRepository universityRepository;
    private final CityRepository cityRepository;

    private final JwtService jwtService;

    public GoogleLoginSuccessData handleGoogleLogin(OAuth2User principal) {
        String email = extractEmail(principal);
        String displayName = extractDisplayName(principal);
        String providerUserId = extractProviderUserId(principal);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            if (!Boolean.TRUE.equals(user.getActive())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is deactivated");
            }

            linkGoogleAccountIfNeeded(user, providerUserId, email);

            String token = jwtService.generateToken(user);

            return GoogleLoginSuccessData.builder()
                    .requiresOnboarding(false)
                    .pendingToken(null)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .displayName(resolveDisplayName(user, displayName))
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationInSeconds())
                    .build();
        }

        String pendingToken = UUID.randomUUID().toString();

        PendingGoogleSignup pendingGoogleSignup = PendingGoogleSignup.builder()
                .token(pendingToken)
                .email(email)
                .displayName(displayName)
                .providerUserId(providerUserId)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        pendingGoogleSignupRepository.save(pendingGoogleSignup);

        return GoogleLoginSuccessData.builder()
                .requiresOnboarding(true)
                .pendingToken(pendingToken)
                .email(email)
                .displayName(displayName)
                .build();
    }

    public AuthResponse completeGoogleSignup(GoogleCompleteSignupRequest request) {
        PendingGoogleSignup pending = pendingGoogleSignupRepository.findByToken(request.getPendingToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired Google signup token"));

        if (pending.getExpiresAt().isBefore(LocalDateTime.now())) {
            pendingGoogleSignupRepository.delete(pending);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google signup token has expired");
        }

        if (userRepository.existsByEmail(pending.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        RoleType role = resolveAccountType(request.getAccountType());

        University university = getUniversityOrThrow(request.getUniversityId());
        City city = getCityOrThrow(request.getCityId());

        User user = User.builder()
                .email(pending.getEmail())
                .password(null)
                .role(role)
                .active(true)
                .emailVerified(true)
                .termsAccepted(Boolean.TRUE.equals(request.getTermsAccepted()))
                .termsAcceptedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        if (role == RoleType.STUDENT) {
            createStudentProfile(user, request, pending, university, city);
        } else if (role == RoleType.ORGANIZER) {
            createOrganizerProfile(user, request, pending, university, city);
        }

        AuthAccount authAccount = AuthAccount.builder()
                .user(user)
                .provider(AuthProvider.GOOGLE)
                .providerUserId(pending.getProviderUserId())
                .providerEmail(pending.getEmail())
                .build();

        authAccountRepository.save(authAccount);

        pendingGoogleSignupRepository.delete(pending);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .displayName(resolveDisplayName(user, pending.getDisplayName()))
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationInSeconds())
                .message("Google signup completed successfully")
                .build();
    }

    private RoleType resolveAccountType(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account type is required");
        }

        String value = accountType.trim().toUpperCase();

        return switch (value) {
            case "STUDENT" -> RoleType.STUDENT;
            case "ORGANIZER" -> RoleType.ORGANIZER;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account type");
        };
    }

    private void createStudentProfile(
            User user,
            GoogleCompleteSignupRequest request,
            PendingGoogleSignup pending,
            University university,
            City city
    ) {
        String fullName = request.getFullName();

        if (fullName == null || fullName.isBlank()) {
            fullName = pending.getDisplayName();
        }

        StudentProfile studentProfile = StudentProfile.builder()
                .user(user)
                .fullName(fullName.trim())
                .gender(request.getGender())
                .university(university)
                .city(city)
                .build();

        studentProfileRepository.save(studentProfile);
    }

    private void createOrganizerProfile(
            User user,
            GoogleCompleteSignupRequest request,
            PendingGoogleSignup pending,
            University university,
            City city
    ) {
        if (request.getOrganizationName() == null || request.getOrganizationName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization name is required");
        }

        String organizationName = request.getOrganizationName().trim();

        if (organizerProfileRepository.existsByOrganizationNameIgnoreCase(organizationName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Organization name is already used"
            );
        }

        if (request.getOrganizationType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization type is required");
        }

        if (request.getShortDescription() == null || request.getShortDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Short description is required");
        }

        OrganizerProfile organizerProfile = OrganizerProfile.builder()
                .user(user)
                .organizationName(organizationName)
                .university(university)
                .city(city)
                .organizationType(request.getOrganizationType())
                .shortDescription(request.getShortDescription().trim())
                .build();

        organizerProfileRepository.save(organizerProfile);
    }

    private University getUniversityOrThrow(Long universityId) {
        if (universityId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "University is required");
        }

        return universityRepository.findById(universityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "University not found"));
    }

    private City getCityOrThrow(Long cityId) {
        if (cityId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City is required");
        }

        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "City not found"));
    }

    private void linkGoogleAccountIfNeeded(User user, String providerUserId, String email) {
        AuthAccount googleAccount = authAccountRepository
                .findByUserIdAndProvider(user.getId(), AuthProvider.GOOGLE)
                .orElse(null);

        if (googleAccount == null) {
            googleAccount = AuthAccount.builder()
                    .user(user)
                    .provider(AuthProvider.GOOGLE)
                    .providerUserId(providerUserId)
                    .providerEmail(email)
                    .build();

            authAccountRepository.save(googleAccount);
        }
    }

    private String extractEmail(OAuth2User principal) {
        String email = principal.getAttribute("email");

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google account email was not provided");
        }

        return email.trim().toLowerCase();
    }

    private String extractDisplayName(OAuth2User principal) {
        String name = principal.getAttribute("name");

        if (name != null && !name.isBlank()) {
            return name.trim();
        }

        return extractEmail(principal);
    }

    private String extractProviderUserId(OAuth2User principal) {
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getSubject();
        }

        return principal.getName();
    }

    private String resolveDisplayName(User user, String fallbackName) {
        return switch (user.getRole()) {
            case STUDENT -> studentProfileRepository.findByUserId(user.getId())
                    .map(StudentProfile::getFullName)
                    .orElse(fallbackName);

            case ORGANIZER -> organizerProfileRepository.findByUserId(user.getId())
                    .map(OrganizerProfile::getOrganizationName)
                    .orElse(fallbackName);

            case ADMIN -> "Admin";
        };
    }
}