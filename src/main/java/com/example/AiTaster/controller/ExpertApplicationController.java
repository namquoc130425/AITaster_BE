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
    // Expert ứng tuyển vào JobPost.
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
  // Thanh toán proposal qua SePay.
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

    // Client sở hữu JobPost xem danh sách expert đã ứng tuyển.
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

    // Client sở hữu hoặc expert sở hữu xem chi tiết application.
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

    // Client mở khóa detailContent của proposal.
    // Trả về nguyên dữ liệu application để FE cập nhật card dễ.
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
