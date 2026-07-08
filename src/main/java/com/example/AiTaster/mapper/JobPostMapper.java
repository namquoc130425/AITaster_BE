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

    // Chuyển GeminiResponse(result) thành JobPost với status Draft.

@Mapping(target = "clientProfile", source = "clientProfile")
@Mapping(target = "title",source = "geminiJobPostResult.title")
@Mapping(target = "requirementDescription",source = "geminiJobPostResult.requirementDescription")
@Mapping(target = "businessGoal",source = "geminiJobPostResult.businessGoal")
@Mapping(target = "mainFeatures",source = "geminiJobPostResult.mainFeatures")
@Mapping(target = "budgets",source = "geminiJobPostResult.budgets")
@Mapping(target = "timeLine",source = "geminiJobPostResult.timeLine")
@Mapping(target = "jobPostStatus",constant = "OPEN")
@Mapping(target = "skills",ignore = true)
    JobPost toEntityJobPostDraft(GeminiJobPostResponse geminiJobPostResult , ClientProfile clientProfile)   ;


    // Dữ liệu client nhập tay.

    @Mapping(target = "clientProfile", source = "clientProfile")
    @Mapping(target = "title",source = "jobPostRequest.title")
    @Mapping(target = "requirementDescription",source = "jobPostRequest.requirementDescription")
    @Mapping(target = "businessGoal",source = "jobPostRequest.businessGoal")
    @Mapping(target = "mainFeatures",source = "jobPostRequest.mainFeatures")
    @Mapping(target = "budgets",source = "jobPostRequest.budgets")
    @Mapping(target = "timeLine",source = "jobPostRequest.timeLine")
    @Mapping(target = "jobPostStatus",constant = "OPEN")
    @Mapping(target = "skills",ignore = true)

    JobPost toEntityJobPostDraft(JobPostRequest jobPostRequest , ClientProfile clientProfile);



    // Chuyển job post sang dữ liệu trả về.
    @Mapping(target = "clientId",source = "clientProfile")
    @Mapping(target = "skills",source = "skills")
    @Mapping(target = "applicationCount", ignore = true)

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
