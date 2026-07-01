package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.ExpertApplicationRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ExpertApplicationResponse;
import com.example.AiTaster.dto.response.SepayPurchasePaymentResponse;
import com.example.AiTaster.service.ExpertApplicationService;
import com.example.AiTaster.service.payment.ProposalPurchaseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class ExpertApplicationController {
    private final ExpertApplicationService expertApplicationService;
    private final ProposalPurchaseService proposalPurchaseService;
    // Expert apply vào JobPost.
    // Có thể gửi kèm proposal hoặc không.
    @PostMapping("/job-posts/{jobPostId}/applications")
    public ResponseEntity<APIResponse<ExpertApplicationResponse>> applyJobPost(
            @PathVariable Long jobPostId,
            @RequestBody @Valid ExpertApplicationRequest request
    ) {
        ExpertApplicationResponse response = expertApplicationService.applyJobPost(jobPostId, request);
        return ResponseEntity.ok(
                APIResponse.response(201, "Apply job post successfully", response)
        );
    }
  // thanh toán proposal qua sepay
    @PostMapping("/proposals/{proposalId}/unlock/sepay")
    public ResponseEntity<APIResponse<SepayPurchasePaymentResponse>> createProposalSepayPayment(
            @PathVariable Long proposalId
    ) {
        SepayPurchasePaymentResponse response =
                proposalPurchaseService.createProposalSepayPayment(proposalId);

        return ResponseEntity.ok(
                APIResponse.response(200, "SePay proposal payment created", response)
        );
    }

    // Client owner của JobPost xem danh sách expert đã apply vào JobPost.
    @GetMapping("/job-posts/{jobPostId}/applications")
    public ResponseEntity<APIResponse<List<ExpertApplicationResponse>>> getApplicationsByJobPost(
            @PathVariable Long jobPostId
    ) {
        List<ExpertApplicationResponse> responses =
                expertApplicationService.getApplicationsByJobPost(jobPostId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get applications successfully", responses)
        );
    }

    // Expert xem danh sách application của mình.
    @GetMapping("/applications/myApplication")
    public ResponseEntity<APIResponse<List<ExpertApplicationResponse>>> getMyApplications() {
        List<ExpertApplicationResponse> responses =
                expertApplicationService.getMyApplications();

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my applications successfully", responses)
        );
    }

    // Client owner hoặc expert owner xem chi tiết application.
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<APIResponse<ExpertApplicationResponse>> getApplicationDetail(
            @PathVariable Long applicationId
    ) {
        ExpertApplicationResponse response =
                expertApplicationService.getApplicationDetail(applicationId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get application detail successfully", response)
        );
    }

    // Client unlock proposal detailContent.
    // Trả về nguyên application response để FE update card dễ.
    @PostMapping("/proposals/{proposalId}/unlock")
    public ResponseEntity<APIResponse<ExpertApplicationResponse>> unlockProposal(
            @PathVariable Long proposalId
    ) {
        ExpertApplicationResponse response =
                expertApplicationService.unlockProposal(proposalId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Unlock proposal successfully", response)
        );
    }
}
