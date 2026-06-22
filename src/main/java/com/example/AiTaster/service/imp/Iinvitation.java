package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.InvitationAcceptRequest;
import com.example.AiTaster.dto.request.InvitationCreateRequest;
import com.example.AiTaster.dto.response.InvitationDraftResponse;
import com.example.AiTaster.dto.response.InvitationResponse;

import java.util.List;

public interface Iinvitation {
    InvitationDraftResponse getDraftByApplication(Long applicationId);
    InvitationResponse createInvitation(InvitationCreateRequest request);
    List<InvitationResponse> getMyClientInvitations();

    List<InvitationResponse> getMyExpertInvitations();

    InvitationResponse getInvitationDetail(Long invitationId);

    InvitationResponse acceptInvitation(Long invitationId, InvitationAcceptRequest request);

    InvitationResponse rejectInvitation(Long invitationId);
}
