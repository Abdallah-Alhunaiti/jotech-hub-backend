package com.jotechhub.lookup;

import com.jotechhub.common.response.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;

    @GetMapping("/cities")
    public ApiSuccessResponse<List<LookupItemResponse>> getCities() {
        return ApiSuccessResponse.of(lookupService.getCities());
    }

    @GetMapping("/universities")
    public ApiSuccessResponse<List<LookupItemResponse>> getUniversities() {
        return ApiSuccessResponse.of(lookupService.getUniversities());
    }

    @GetMapping("/categories")
    public ApiSuccessResponse<List<LookupItemResponse>> getCategories() {
        return ApiSuccessResponse.of(lookupService.getCategories());
    }

    @GetMapping("/tags")
    public ApiSuccessResponse<List<LookupItemResponse>> getTags() {
        return ApiSuccessResponse.of(lookupService.getTags());
    }

    @GetMapping("/organization-types")
    public ApiSuccessResponse<List<OrganizationTypeResponse>> getOrganizationTypes() {
        return ApiSuccessResponse.of(lookupService.getOrganizationTypes());
    }
}