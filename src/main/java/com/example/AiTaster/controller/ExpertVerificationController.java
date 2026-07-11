package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.ResubmitExpertCertificateRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ExpertVerificationResponse;
import com.example.AiTaster.service.ExpertProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expert/verification")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EXPERT')")
@SecurityRequirement(name = "api")
public class ExpertVerificationController {

    private final ExpertProfileService expertProfileService;

    @PatchMapping("/resubmit")
    public ResponseEntity<APIResponse<ExpertVerificationResponse>> resubmit(
            @RequestBody @Valid ResubmitExpertCertificateRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Gửi lại chứng chỉ chuyên gia thành công",
                        expertProfileService.resubmitCertificate(request)
                )
        );
    }
}
