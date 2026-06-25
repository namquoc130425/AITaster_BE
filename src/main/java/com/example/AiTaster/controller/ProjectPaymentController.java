package com.example.AiTaster.controller;

import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.service.ProjectPaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class ProjectPaymentController {
    private final ProjectPaymentService projectPaymentService;

    @PostMapping("/{projectId}/payments")
    public ResponseEntity<APIResponse<ProjectPaymentResponse>> createProjectPayment(
            @PathVariable Long projectId
    ) {
        ProjectPaymentResponse response = projectPaymentService.createProjectPayment(projectId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Create project payment successfully", response)
        );
    }
}
