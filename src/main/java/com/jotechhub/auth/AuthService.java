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
import org.springframework.beans.factory.annotation.Value;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import com.jotechhub.notification.EmailService;

import java.time.LocalDateTime;

import com.jotechhub.security.JwtService;
import com.jotechhub.organizer.OrganizerImageStorageService;
import org.springframework.web.multipart.MultipartFile;


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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final OrganizerImageStorageService organizerImageStorageService;

    @Value("${app.frontend.password-reset-url}")
    private String passwordResetUrl;

    @Value("${app.auth.password-reset-expiration-minutes}")
    private long passwordResetExpirationMinutes;

    @Value("${app.auth.expose-password-reset-token:false}")
    private boolean exposePasswordResetToken;

    public AuthResponse signupOrganizer(OrganizerSignupRequest request) {
        return signupOrganizer(request, null);
    }
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
                .gender(request.getGender())
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

    public AuthResponse signupOrganizer(OrganizerSignupRequest request, MultipartFile organizationImage) {
        System.out.println("signupOrganizer service reached");

        validateEmailNotUsed(request.getEmail());
        validatePasswords(request.getPassword(), request.getConfirmPassword());
        validateOrganizationNameNotUsed(request.getOrganizationName());

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

        String organizationImageUrl = organizerImageStorageService.storeOrganizerImage(
                organizationImage,
                user.getId()
        );

        OrganizerProfile organizerProfile = OrganizerProfile.builder()
                .user(user)
                .organizationName(request.getOrganizationName().trim())
                .organizationImageUrl(organizationImageUrl)
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
                .organizationImageUrl(organizerProfile.getOrganizationImageUrl())
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

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String genericMessage = "If this email exists and supports password login, a password reset link has been generated.";

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !Boolean.TRUE.equals(user.getActive())) {
            return ForgotPasswordResponse.builder()
                    .message(genericMessage)
                    .build();
        }

        boolean hasLocalAccount = authAccountRepository
                .findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL)
                .isPresent();

        if (!hasLocalAccount || user.getPassword() == null) {
            return ForgotPasswordResponse.builder()
                    .message(genericMessage)
                    .build();
        }

        passwordResetTokenRepository.deleteByUserIdAndUsedAtIsNull(user.getId());

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes))
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        String resetLink = passwordResetUrl + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        return ForgotPasswordResponse.builder()
                .message(genericMessage)
                .resetToken(exposePasswordResetToken ? rawToken : null)
                .resetLink(exposePasswordResetToken ? resetLink : null)
                .build();
    }

    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        validatePasswords(request.getNewPassword(), request.getConfirmPassword());

        String tokenHash = hashToken(request.getToken());

        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNull(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or expired password reset token"
                ));

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetToken.setUsedAt(LocalDateTime.now());
            passwordResetTokenRepository.save(passwordResetToken);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired password reset token"
            );
        }

        User user = passwordResetToken.getUser();

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is deactivated");
        }

        boolean hasLocalAccount = authAccountRepository
                .findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL)
                .isPresent();

        if (!hasLocalAccount) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account does not support password reset"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(passwordResetToken);

        return ResetPasswordResponse.builder()
                .message("Password has been reset successfully")
                .build();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not process reset token"
            );
        }
    }

    private void validateOrganizationNameNotUsed(String organizationName) {
        if (organizerProfileRepository.existsByOrganizationNameIgnoreCase(organizationName.trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Organization name is already used"
            );
        }
    }
}