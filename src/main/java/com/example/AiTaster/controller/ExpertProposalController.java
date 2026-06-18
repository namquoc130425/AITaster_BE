//package com.example.AiTaster.controller;
//import com.example.AiTaster.dto.request.ExpertProposalRequest;
//import com.example.AiTaster.dto.response.APIResponse;
//import com.example.AiTaster.dto.response.ExpertProposalPreviewResponse;
//import com.example.AiTaster.dto.response.ExpertProposalResponse;
//import com.example.AiTaster.service.ExpertProposalService;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api")
//@CrossOrigin("*")
//@SecurityRequirement(name = "api")
//@RequiredArgsConstructor
//public class ExpertProposalController {
//    private final ExpertProposalService expertProposalService;
//
//    @PostMapping("/job-posts/{jobPostId}/proposals")
//    public ResponseEntity<APIResponse<ExpertProposalResponse>> createProposal(
//            @PathVariable Long jobPostId,
//            @RequestBody @Valid ExpertProposalRequest request
//    ) {
//        ExpertProposalResponse response = expertProposalService.createProposal(jobPostId, request);
//        return ResponseEntity.ok(APIResponse.response(201, "Create proposal successfully", response));
//    }
//
//    @GetMapping("/job-posts/{jobPostId}/proposals")
//    public ResponseEntity<APIResponse<List<ExpertProposalPreviewResponse>>> getProposalsByJobPost(
//            @PathVariable Long jobPostId
//    ) {
//        List<ExpertProposalPreviewResponse> responses = expertProposalService.getProposalsByJobPost(jobPostId);
//        return ResponseEntity.ok(APIResponse.response(200, "Get proposals successfully", responses));
//    }
//
//    @GetMapping("/proposals/my")
//    public ResponseEntity<APIResponse<List<ExpertProposalPreviewResponse>>> getMyProposals() {
//        List<ExpertProposalPreviewResponse> responses = expertProposalService.getMyProposals();
//        return ResponseEntity.ok(APIResponse.response(200, "Get my proposals successfully", responses));
//    }
//
//    @GetMapping("/proposals/{proposalId}")
//    public ResponseEntity<APIResponse<ExpertProposalResponse>> getProposalDetail(
//            @PathVariable Long proposalId
//    ) {
//        ExpertProposalResponse response = expertProposalService.getProposalDetail(proposalId);
//        return ResponseEntity.ok(APIResponse.response(200, "Get proposal detail successfully", response));
//    }
//
//    @PutMapping("/proposals/{proposalId}")
//    public ResponseEntity<APIResponse<ExpertProposalResponse>> updateProposal(
//            @PathVariable Long proposalId,
//            @RequestBody @Valid ExpertProposalRequest request
//    ) {
//        ExpertProposalResponse response = expertProposalService.updateProposal(proposalId, request);
//        return ResponseEntity.ok(APIResponse.response(200, "Update proposal successfully", response));
//    }
//
//    @DeleteMapping("/proposals/delete/{proposalId}")
//    public ResponseEntity<APIResponse<Void>> deleteProposal(@PathVariable Long proposalId) {
//        expertProposalService.deleteProposal(proposalId);
//        return ResponseEntity.ok(APIResponse.response(200, "Delete proposal successfully", null));
//    }
//
//    @PostMapping("/proposals/{proposalId}/unlock")
//    public ResponseEntity<APIResponse<ExpertProposalResponse>> unlockProposal(
//            @PathVariable Long proposalId
//    ) {
//        ExpertProposalResponse response = expertProposalService.unlockProposal(proposalId);
//        return ResponseEntity.ok(APIResponse.response(200, "Unlock proposal successfully", response));
//    }
//}
