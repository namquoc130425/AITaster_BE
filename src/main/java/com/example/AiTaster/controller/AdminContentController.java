package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.Admin.AdminContentModerationRequest;
import com.example.AiTaster.dto.request.Admin.AdminExpertServiceFilterRequest;
import com.example.AiTaster.dto.request.Admin.AdminJobPostFilterRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.service.AdminContentModerationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "api")
public class AdminContentController {
    private final AdminContentModerationService adminContentModerationService;

    @PostMapping("/job-posts/filter")
    public ResponseEntity<APIResponse<PageResponse<JobPostResponse>>> filterJobPosts(
            @RequestBody(required = false) AdminJobPostFilterRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get admin job posts successfully",
                        adminContentModerationService.filterJobPosts(request)
                )
        );
    }

    @PatchMapping("/job-posts/{jobPostId}/remove")
    public ResponseEntity<APIResponse<JobPostResponse>> removeJobPost(
            @PathVariable Long jobPostId,
            @RequestBody(required = false) @Valid AdminContentModerationRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Remove job post successfully",
                        adminContentModerationService.removeJobPost(jobPostId, request)
                )
        );
    }

    @PostMapping("/ai-services/filter")
    public ResponseEntity<APIResponse<PageResponse<ExpertServiceResponse>>> filterExpertServices(
            @RequestBody(required = false) AdminExpertServiceFilterRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get admin AI services successfully",
                        adminContentModerationService.filterExpertServices(request)
                )
        );
    }

    @PatchMapping("/ai-services/{serviceId}/remove")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> removeExpertService(
            @PathVariable Long serviceId,
            @RequestBody(required = false) @Valid AdminContentModerationRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Remove AI service successfully",
                        adminContentModerationService.removeExpertService(serviceId, request)
                )
        );
    }
}
