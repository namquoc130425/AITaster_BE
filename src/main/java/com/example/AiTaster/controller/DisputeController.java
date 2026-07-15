package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.CreateDisputeRequest;
import com.example.AiTaster.dto.request.DisputeFilterRequest;
import com.example.AiTaster.dto.request.ResolveDisputeRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.DisputeResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.service.DisputeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/disputes")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class DisputeController {
    private final DisputeService disputeService;

    @PostMapping("/projects/{projectId}")
    public ResponseEntity<APIResponse<DisputeResponse>> create(
            @PathVariable Long projectId,
            @RequestBody @Valid CreateDisputeRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        201,
                        "Create dispute successfully",
                        disputeService.create(projectId, request)
                )
        );
    }

    @PostMapping("/admin/filter")
    public ResponseEntity<APIResponse<PageResponse<DisputeResponse>>> filterAdmin(
            @RequestBody(required = false) DisputeFilterRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Filter disputes successfully",
                        disputeService.filterAdmin(request)
                )
        );
    }

    @PatchMapping("/admin/{disputeId}/resolve")
    public ResponseEntity<APIResponse<DisputeResponse>> resolve(
            @PathVariable Long disputeId,
            @RequestBody @Valid ResolveDisputeRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Resolve dispute successfully",
                        disputeService.resolve(disputeId, request)
                )
        );
    }
}
