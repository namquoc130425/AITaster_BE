package com.example.AiTaster.mapper;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.dto.request.InvitationCreateRequest;
import com.example.AiTaster.dto.response.InvitationDraftResponse;
import com.example.AiTaster.dto.response.InvitationResponse;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface InvitationMapper {
    @Mapping(target = "applicationId", source = "expertApplication.applicationId")
    @Mapping(target = "projectTitle", source = "expertApplication.jobpost.title")
    @Mapping(target = "finalRequirement", source = "expertApplication.jobpost.requirementDescription")
    @Mapping(target = "expectedOutput", source = "expertApplication.jobpost.mainFeatures")
    @Mapping(target = "acceptanceCriteria", constant = "")
    @Mapping(target = "finalOfferedPrice", source = "expertApplication.expectedPrice")
    @Mapping(target = "clientAcceptedTerms", constant = "false")
    InvitationDraftResponse toResponse(ExpertApplication expertApplication);


    @Mapping(target = "expertApplication", source = "expertApplication")
    @Mapping(target = "expertAcceptedTerms", constant = "false")
    @Mapping(target = "invitationStatus", constant = "PENDING")
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "respondedAt", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    Invitation toEntity(InvitationCreateRequest request, ExpertApplication expertApplication);


    @Mapping(target = "applicationId", source = "expertApplication.applicationId")
    @Mapping(target = "jobPostId", source = "expertApplication.jobpost.jobPostId")
    @Mapping(target = "jobPostTitle", source = "expertApplication.jobpost.title")

    @Mapping(target = "clientProfileId", source = "expertApplication.jobpost.clientProfile.clientProfileId")
    @Mapping(target = "companyName", source = "expertApplication.jobpost.clientProfile.companyName")
    @Mapping(target = "contactName", source = "expertApplication.jobpost.clientProfile.contactName")

    @Mapping(target = "expertProfileId", source = "expertApplication.expertProfile.expertProfileId")
    @Mapping(target = "expertName", source = "expertApplication.expertProfile.user.fullName")
    @Mapping(target = "paymentDeadline", expression = "java(buildPaymentDeadline(invitation))")
        // paymentDeadline không có sẵn trong entity, nên phải tự tính bằng helper.
    InvitationResponse toResponseInvitation(Invitation invitation);

    default LocalDateTime buildPaymentDeadline(Invitation invitation) {
        if (invitation == null) {
            return null;
        }
        if (!InvitationStatus.ACCEPTED.equals(invitation.getInvitationStatus())) {
            return null;
        }
        if (invitation.getRespondedAt() == null) {
            return null; // Nếu thiếu thời điểm accept thì không thể tính deadline.
        }

        return invitation.getRespondedAt().plusHours(24); // Hạn thanh toán = lúc expert accept + 24 giờ.
    }


}
