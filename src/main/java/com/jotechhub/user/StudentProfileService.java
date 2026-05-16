package com.jotechhub.user;

import com.jotechhub.city.City;
import com.jotechhub.city.CityRepository;
import com.jotechhub.role.RoleType;
import com.jotechhub.university.University;
import com.jotechhub.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UniversityRepository universityRepository;
    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public StudentProfileResponse getMyProfile(Authentication authentication) {
        User student = getCurrentStudent(authentication);

        StudentProfile profile = studentProfileRepository.findByUserId(student.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));

        return mapToResponse(student, profile);
    }

    public StudentProfileResponse updateMyProfile(
            UpdateStudentProfileRequest request,
            Authentication authentication
    ) {
        User student = getCurrentStudent(authentication);

        StudentProfile profile = studentProfileRepository.findByUserId(student.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));

        University university = universityRepository.findById(request.getUniversityId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Selected university was not found"
                ));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Selected city was not found"
                ));

        profile.setFullName(request.getFullName().trim());
        profile.setGender(request.getGender());
        profile.setUniversity(university);
        profile.setCity(city);

        profile = studentProfileRepository.save(profile);

        return mapToResponse(student, profile);
    }

    private User getCurrentStudent(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is deactivated");
        }

        if (user.getRole() != RoleType.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only students can manage student profile"
            );
        }

        return user;
    }

    private StudentProfileResponse mapToResponse(User user, StudentProfile profile) {
        return StudentProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(profile.getFullName())
                .gender(profile.getGender() != null ? profile.getGender().name() : null)
                .universityId(profile.getUniversity() != null ? profile.getUniversity().getId() : null)
                .universityName(profile.getUniversity() != null ? profile.getUniversity().getName() : null)
                .cityId(profile.getCity() != null ? profile.getCity().getId() : null)
                .cityName(profile.getCity() != null ? profile.getCity().getName() : null)
                .build();
    }
}