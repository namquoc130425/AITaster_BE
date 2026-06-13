package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ExpertProposalRequest;
import com.example.AiTaster.dto.response.ExpertProposalPreviewResponse;
import com.example.AiTaster.dto.response.ExpertProposalResponse;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertProposal;
import com.example.AiTaster.entity.JobPost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExpertProposalMapper {

    @Mapping(target = "jobpost", source = "jobpost")
    @Mapping(target = "expertProfile", source = "expertProfile")
    @Mapping(target = "title", source = "proposalRequest.title")
    @Mapping(target = "summary", source = "proposalRequest.summary")
    @Mapping(target = "technologies", source = "proposalRequest.technologies")
    @Mapping(target = "detailContent", source = "proposalRequest.detailContent")
    @Mapping(target = "priceToUnlock", source = "proposalRequest.priceToUnlock")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)

    ExpertProposal toEntity(ExpertProposalRequest  proposalRequest, JobPost jobpost, ExpertProfile expertProfile);


    // Map sang response preview, không có detailContent.
    @Mapping(target = "jobPostId", source = "proposal.jobpost.jobPostId")
    @Mapping(target = "expertProfileId", source = "proposal.expertProfile.expertProfileId")
    @Mapping(target = "expertName", source = "proposal.expertProfile.user.fullName")
    @Mapping(target = "isUnlocked", source = "isUnlocked")
    ExpertProposalPreviewResponse toPreviewResponse(ExpertProposal proposal,Boolean isUnlocked);


    // Map sang response detail.
    @Mapping(target = "jobPostId", source = "proposal.jobpost.jobPostId")

    @Mapping(target = "expertProfileId", source = "proposal.expertProfile.expertProfileId")
    @Mapping(target = "expertName", source = "proposal.expertProfile.user.fullName")
    @Mapping(target = "detailContent", source = "detailContent")
    // detailContent là tham số service truyền vào.
    // Nếu chưa unlock thì service truyền null.
    // Nếu đã unlock thì service truyền proposal.getDetailContent().
    @Mapping(target = "isUnlocked", source = "isUnlocked")
    ExpertProposalResponse toResponse(ExpertProposal proposal, String detailContent, Boolean isUnlocked);



    @Mapping(target = "proposalId", ignore = true)
    @Mapping(target = "jobpost", ignore = true)
    @Mapping(target = "expertProfile", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void UpdateEntiy(ExpertProposalRequest request,@MappingTarget ExpertProposal proposal);




}
