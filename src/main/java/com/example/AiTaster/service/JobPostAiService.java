package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.GeminiJobPostResponse;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.JobPostRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobPostAiService {
    private final GeminiClientService geminiClientService;
    private final CurrentUserService currentUserService;
    private final JobPostRepo  jobPostRepo;
    private final JobPostMapper jobPostMapper;
    private final ContentManagerService contentManagerService;

    private final ClientProfileRepo clientProfileRepo;

    public JobPostResponse CreatJobPostByAi(JobPostAiRequest jobPostAiRequest) throws JsonProcessingException {
        // 1. lấy user đã login
      User userCurrent =  currentUserService.getCurrentUser();
       // 2.  từ user tìm client profile
        ClientProfile clientProfile = clientProfileRepo.findByUser(userCurrent).orElseThrow(() -> new GlobalException("User not found"));
       // 2,5 .
       contentManagerService.validateKeywordInput(jobPostAiRequest.getKeyword());
        // 3. gọi gemini ra dữ liệu
     GeminiJobPostResponse geminiJobPostResponse = geminiClientService.generateJobPost(jobPostAiRequest.getKeyword());
        //4. gọi validate và chuẩn quá dữ liệu
     validateAiResponse(geminiJobPostResponse);
        // map result(responseAi) thành enitty
        JobPost jobPost = jobPostMapper.toEntityJobPostDraft(geminiJobPostResponse,clientProfile);
        //lưu xuống db
        JobPost saveJobpost = jobPostRepo.save(jobPost);
        // rặn cho frontend
        return jobPostMapper.toResponse(saveJobpost);
    }

    private void validateAiResponse(GeminiJobPostResponse geminiJobPostResponse) {
        if (geminiJobPostResponse == null) { //  không trả object
            throw new GlobalException("AI không tạo được dữ liệu job post");
        }

        if (geminiJobPostResponse.getTitle() == null || geminiJobPostResponse.getTitle().isBlank()) { // Nếu thiếu title
            throw new GlobalException("AI trả thiếu title");
        }

        if (geminiJobPostResponse.getRequirementDescription() == null ||geminiJobPostResponse.getRequirementDescription().isBlank()) { // Nếu thiếu mô tả
            throw new GlobalException("AI trả thiếu requirementDescription");
        }

        if (geminiJobPostResponse.getBusinessGoal() == null || geminiJobPostResponse.getBusinessGoal().isBlank()) { // Nếu thiếu businessGoal
            throw new GlobalException("AI trả thiếu businessGoal");
        }

        if (geminiJobPostResponse.getMainFeatures() == null || geminiJobPostResponse.getMainFeatures().isBlank()) { // Nếu thiếu mainFeatures
            throw new GlobalException("AI trả thiếu mainFeatures");
        }
// target này ko bt làm fild gì nên chưa làm
//        if (geminiJobPostResponse.getTargetUsers() == null || geminiJobPostResponse.getTargetUsers().isBlank()) { // Nếu thiếu targetUsers
//            throw new GlobalException("AI trả thiếu targetUsers");
//        }


        if (geminiJobPostResponse.getRequiredSkills() == null || geminiJobPostResponse.getRequiredSkills().isBlank()) { // Nếu thiếu skill
            throw new GlobalException("AI trả thiếu requiredSkills");
        }

        if (geminiJobPostResponse.getBudgets() == null) { // Nếu thiếu budget
            throw new GlobalException("AI trả thiếu budgets");
        }

        if (geminiJobPostResponse.getTimeLine() == null || geminiJobPostResponse.getTimeLine().isBlank()) { // Nếu thiếu timeline
            throw new GlobalException("AI trả thiếu timeLine");
        }
    }
}
