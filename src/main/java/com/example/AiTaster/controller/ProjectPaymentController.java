package com.example.AiTaster.controller;

import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.service.ProjectPaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class ProjectPaymentController {
    private final ProjectPaymentService projectPaymentService;

    @PostMapping("/{invitationId}/payments")
    public ResponseEntity<APIResponse<ProjectPaymentResponse>> createProjectPayment(
            @PathVariable Long invitationId
    ) {
        ProjectPaymentResponse response = projectPaymentService.createProjectPayment(invitationId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Create invitation payment successfully", response)
        );
    }
}
