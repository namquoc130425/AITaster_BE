package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ExpertApplicationRequest;
import com.example.AiTaster.dto.response.ExpertApplicationResponse;
import com.example.AiTaster.dto.response.ExpertProposalResponse;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertProposal;
import com.example.AiTaster.entity.JobPost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpertApplicationMapper {

    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "jobpost", source = "jobpost")
    @Mapping(target = "expertProfile", source = "expertProfile")
    @Mapping(target = "expertProposal", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    ExpertApplication toEntity(ExpertApplicationRequest expertApplicationRequest , JobPost jobpost , ExpertProfile expertProfile);

    @Mapping(target = "applicationId", source = "expertApplication.applicationId")
    @Mapping(target = "jobPostId", source = "expertApplication.jobpost.jobPostId")
    @Mapping(target = "expertProfileId", source = "expertApplication.expertProfile.expertProfileId")
    @Mapping(target = "expertName", source = "expertApplication.expertProfile.user.fullName")
    @Mapping(target = "proposal", source = "expertProposal")
    @Mapping(target = "createAt", source = "expertApplication.createAt")
    @Mapping(target = "updateAt", source = "expertApplication.updateAt")
    ExpertApplicationResponse toResponse(ExpertApplication expertApplication, ExpertProposalResponse expertProposal);
}
