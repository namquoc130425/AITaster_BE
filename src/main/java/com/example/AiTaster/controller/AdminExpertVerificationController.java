package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.RejectExpertVerificationRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ExpertVerificationResponse;
import com.example.AiTaster.service.AdminExpertVerificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/expert-verifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "api")
public class AdminExpertVerificationController {

    private final AdminExpertVerificationService service;

    @GetMapping("/submitted")
    public ResponseEntity<APIResponse<List<ExpertVerificationResponse>>> getSubmittedVerifications() {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get submitted expert verifications successfully",
                        service.getSubmittedVerifications()
                )
        );
    }

    @PatchMapping("/{verificationId}/approve")
    public ResponseEntity<APIResponse<ExpertVerificationResponse>> approve(
            @PathVariable Long verificationId
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Expert verification approved successfully",
                        service.approve(verificationId)
                )
        );
    }

    @PatchMapping("/{verificationId}/reject")
    public ResponseEntity<APIResponse<ExpertVerificationResponse>> reject(
            @PathVariable Long verificationId,
            @RequestBody @Valid RejectExpertVerificationRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Expert verification rejected successfully",
                        service.reject(verificationId, request.getReason())
                )
        );
    }
}
