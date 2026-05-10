package com.jotechhub.auth;

import com.jotechhub.role.RoleType;
import com.jotechhub.security.JwtService;
import com.jotechhub.user.StudentProfile;
import com.jotechhub.user.StudentProfileRepository;
import com.jotechhub.user.User;
import com.jotechhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleOAuth2Service {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JwtService jwtService;

    public GoogleLoginSuccessData handleGoogleLogin(OAuth2User principal) {
        String email = extractEmail(principal);
        String displayName = extractDisplayName(principal);
        String providerUserId = extractProviderUserId(principal);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = User.builder()
                    .email(email)
                    .password(null)
                    .role(RoleType.STUDENT)
                    .active(true)
                    .emailVerified(true)
                    .termsAccepted(false)
                    .termsAcceptedAt(null)
                    .build();

            user = userRepository.save(user);

            StudentProfile studentProfile = StudentProfile.builder()
                    .user(user)
                    .fullName(displayName)
                    .university(null)
                    .city(null)
                    .build();

            studentProfileRepository.save(studentProfile);
        } else {
            if (!Boolean.TRUE.equals(user.getActive())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is deactivated");
            }
        }

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

        String token = jwtService.generateToken(user);

        return GoogleLoginSuccessData.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .displayName(resolveDisplayName(user, displayName))
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationInSeconds())
                .build();
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
            case ORGANIZER, ADMIN -> fallbackName;
        };
    }
}