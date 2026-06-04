package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.Ai.GeminiJobPostResponse;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface JobPostMapper {

    //GeminiResponse(result) -> Jobpost với status :Draft
@Mapping(target = "jobPostId",ignore = true)
@Mapping(target = "clientProfile", source = "clientProfile")
@Mapping(target = "title",source = "geminiJobPostResult.title")
@Mapping(target = "requirementDescription",source = "geminiJobPostResult.requirementDescription")
@Mapping(target = "businessGoal",source = "geminiJobPostResult.businessGoal")
@Mapping(target = "mainFeatures",source = "geminiJobPostResult.mainFeatures")
@Mapping(target = "requiredSkills",source = "geminiJobPostResult.requiredSkills")
@Mapping(target = "budgets",source = "geminiJobPostResult.budgets")
@Mapping(target = "timeLine",source = "geminiJobPostResult.timeLine")
@Mapping(target = "jobPostStatus",constant = "DRAFT")
    JobPost toEntityJobPostDraft(GeminiJobPostResponse geminiJobPostResult , ClientProfile clientProfile);


    // dữ lieu nhap tay cua client
    @Mapping(target = "jobPostId",ignore = true)
    @Mapping(target = "clientProfile", source = "clientProfile")
    @Mapping(target = "title",source = "jobPostRequest.title")
    @Mapping(target = "requirementDescription",source = "jobPostRequest.requirementDescription")
    @Mapping(target = "businessGoal",source = "jobPostRequest.businessGoal")
    @Mapping(target = "mainFeatures",source = "jobPostRequest.mainFeatures")
    @Mapping(target = "requiredSkills",source = "jobPostRequest.requiredSkills")
    @Mapping(target = "budgets",source = "jobPostRequest.budgets")
    @Mapping(target = "timeLine",source = "jobPostRequest.timeLine")
    @Mapping(target = "jobPostStatus",constant = "DRAFT")
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)

    JobPost toEntityJobPostDraft(JobPostRequest jobPostRequest , ClientProfile clientProfile);



    // jobpost -> response
    @Mapping(target = "clientId",source = "clientProfile")
    JobPostResponse toResponse(JobPost jobPost);
    @Mapping(target = "jobPostId", ignore = true)
    @Mapping(target = "clientProfile", ignore = true)
    @Mapping(target = "jobPostStatus", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    void updateEntity(JobPostRequest jobPostRequest, @MappingTarget JobPost jobPost);

    default Long mapClientProfiletoClientId(ClientProfile clientProfile) {
        if(clientProfile == null) {
            return null;
        }
        return clientProfile.getClientProfileId();
    }
}
