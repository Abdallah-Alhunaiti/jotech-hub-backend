package com.jotechhub.lookup;

import com.jotechhub.category.CategoryRepository;
import com.jotechhub.city.CityRepository;
import com.jotechhub.organizer.OrganizationType;
import com.jotechhub.tag.TagRepository;
import com.jotechhub.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LookupService {

    private final CityRepository cityRepository;
    private final UniversityRepository universityRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public List<LookupItemResponse> getCities() {
        return cityRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(city -> LookupItemResponse.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .build())
                .toList();
    }

    public List<LookupItemResponse> getUniversities() {
        return universityRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(university -> LookupItemResponse.builder()
                        .id(university.getId())
                        .name(university.getName())
                        .build())
                .toList();
    }

    public List<LookupItemResponse> getCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(category -> LookupItemResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .toList();
    }

    public List<LookupItemResponse> getTags() {
        return tagRepository.findAllByOrderByNameAsc()
                .stream()
                .map(tag -> LookupItemResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .toList();
    }

    public List<OrganizationTypeResponse> getOrganizationTypes() {
        return Arrays.stream(OrganizationType.values())
                .map(type -> OrganizationTypeResponse.builder()
                        .value(type.name())
                        .label(getOrganizationTypeLabel(type))
                        .build())
                .toList();
    }

    private String getOrganizationTypeLabel(OrganizationType type) {
        return switch (type) {
            case UNI_CLUB -> "University Club";
            case COMPANY -> "Company";
            case COMMUNITY -> "Community";
        };
    }
}