package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.InvitationAcceptRequest;
import com.example.AiTaster.dto.request.InvitationCreateRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.InvitationDraftResponse;
import com.example.AiTaster.dto.response.InvitationResponse;
import com.example.AiTaster.service.InvitationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    // Client lấy dữ liệu draft để đổ lên form tạo lời mời.
    // Không lưu DB.
    @GetMapping("/draft/application/{applicationId}")
    public ResponseEntity<APIResponse<InvitationDraftResponse>> getDraftByApplication(
            @PathVariable Long applicationId
    ) {
        InvitationDraftResponse response =
                invitationService.getDraftByApplication(applicationId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get invitation draft successfully", response)
        );
    }

    // Client gửi lời mời cho expert từ ExpertApplication.
    @PostMapping
    public ResponseEntity<APIResponse<InvitationResponse>> createInvitation(
            @RequestBody @Valid InvitationCreateRequest request
    ) {
        InvitationResponse response =
                invitationService.createInvitation(request);

        return ResponseEntity.ok(
                APIResponse.response(201, "Create invitation successfully", response)
        );
    }

    // Client xem danh sách lời mời mình đã gửi.
    @GetMapping("/client/my")
    public ResponseEntity<APIResponse<List<InvitationResponse>>> getMyClientInvitations() {
        List<InvitationResponse> responses =
                invitationService.getMyClientInvitations();

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my client invitations successfully", responses)
        );
    }

    // Expert xem danh sách lời mời mình nhận được.
    @GetMapping("/expert/my")
    public ResponseEntity<APIResponse<List<InvitationResponse>>> getMyExpertInvitations() {
        List<InvitationResponse> responses =
                invitationService.getMyExpertInvitations();

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my expert invitations successfully", responses)
        );
    }

    // Client owner hoặc expert được mời xem chi tiết lời mời.
    @GetMapping("/{invitationId}")
    public ResponseEntity<APIResponse<InvitationResponse>> getInvitationDetail(
            @PathVariable Long invitationId
    ) {
        InvitationResponse response =
                invitationService.getInvitationDetail(invitationId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get invitation detail successfully", response)
        );
    }

    // Expert chấp nhận lời mời.
    // Body bắt buộc expertAcceptedTerms = true.
    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<APIResponse<InvitationResponse>> acceptInvitation(
            @PathVariable Long invitationId,
            @RequestBody @Valid InvitationAcceptRequest request
    ) {
        InvitationResponse response =
                invitationService.acceptInvitation(invitationId, request);

        return ResponseEntity.ok(
                APIResponse.response(200, "Accept invitation successfully", response)
        );
    }

    // Expert từ chối lời mời.
    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<APIResponse<InvitationResponse>> rejectInvitation(
            @PathVariable Long invitationId
    ) {
        InvitationResponse response =
                invitationService.rejectInvitation(invitationId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Reject invitation successfully", response)
        );
    }
}
