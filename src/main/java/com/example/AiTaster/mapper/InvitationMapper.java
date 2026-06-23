package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.InvitationCreateRequest;
import com.example.AiTaster.dto.response.InvitationDraftResponse;
import com.example.AiTaster.dto.response.InvitationResponse;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvitationMapper {
@Mapping(target = "applicationId",source = "expertApplication.applicationId")
@Mapping(target = "projectTitle",source = "expertApplication.jobpost.title")
@Mapping(target = "finalRequirement",source = "expertApplication.jobpost.requirementDescription")
@Mapping(target = "expectedOutput",source = "expertApplication.jobpost.mainFeatures")
@Mapping(target = "acceptanceCriteria",constant = "")
@Mapping(target = "finalOfferedPrice",source = "expertApplication.expectedPrice")
@Mapping(target = "clientAcceptedTerms", constant = "false")
    InvitationDraftResponse toResponse(ExpertApplication expertApplication);



    @Mapping(target = "expertApplication",source = "expertApplication")
    @Mapping(target = "expertAcceptedTerms", constant = "false")
    @Mapping(target = "invitationStatus", constant = "PENDING")
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "respondedAt", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)

Invitation toEntity(InvitationCreateRequest request ,ExpertApplication expertApplication);


    @Mapping(target = "applicationId", source = "expertApplication.applicationId")
    @Mapping(target = "jobPostId", source = "expertApplication.jobpost.jobPostId")
    @Mapping(target = "jobPostTitle", source = "expertApplication.jobpost.title")

    @Mapping(target = "clientProfileId", source = "expertApplication.jobpost.clientProfile.clientProfileId")
    @Mapping(target = "companyName", source = "expertApplication.jobpost.clientProfile.companyName")
    @Mapping(target = "contactName", source = "expertApplication.jobpost.clientProfile.contactName")

    @Mapping(target = "expertProfileId", source = "expertApplication.expertProfile.expertProfileId")
    @Mapping(target = "expertName", source = "expertApplication.expertProfile.user.fullName")
InvitationResponse toResponseInvitation(Invitation invitation);


}
