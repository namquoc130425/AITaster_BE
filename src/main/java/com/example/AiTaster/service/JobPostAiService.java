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
      User userCurrent =  currentUserService.getCurrentUser();
       // 2.  từ user tìm client profile
        ClientProfile clientProfile = clientProfileRepo.findByUser(userCurrent).orElseThrow(() -> new GlobalException("ClientProflie not found"));

       contentManagerService.validateKeywordInput(jobPostAiRequest.getKeyword());

   // thêm hàm lọc skill Ai trả về có nằm trong database hay không nếu có thì mới gữi cho AI còn không có thì bỏ đi để tránh việc AI trả về skill ko có trong database

        // 3. gọi gemini ra dữ liệu
     GeminiJobPostResponse geminiJobPostResponse = geminiClientService.generateJobPost(jobPostAiRequest.getKeyword());
        //4. gọi validate và chuẩn quá dữ liệu
     validateAiResponse(geminiJobPostResponse);




        JobPost jobPost = jobPostMapper.toEntityJobPostDraft(geminiJobPostResponse,clientProfile);
        JobPost saveJobpost = jobPostRepo.save(jobPost);
        return jobPostMapper.toResponse(saveJobpost);
    }

    // thêm hàm kiểm tra dử liệu trước khi gữi cho AI


    private void validateAiResponse(GeminiJobPostResponse geminiJobPostResponse) {
        if (geminiJobPostResponse == null) {
            throw new GlobalException("AI không tạo được dữ liệu job post");
        }

        if (geminiJobPostResponse.getTitle() == null || geminiJobPostResponse.getTitle().isBlank()) {
            throw new GlobalException("AI trả thiếu title");
        }

        if (geminiJobPostResponse.getRequirementDescription() == null ||geminiJobPostResponse.getRequirementDescription().isBlank()) {
            throw new GlobalException("AI trả thiếu requirementDescription");
        }

        if (geminiJobPostResponse.getBusinessGoal() == null || geminiJobPostResponse.getBusinessGoal().isBlank()) {
            throw new GlobalException("AI trả thiếu businessGoal");
        }

        if (geminiJobPostResponse.getMainFeatures() == null || geminiJobPostResponse.getMainFeatures().isBlank()) {
            throw new GlobalException("AI trả thiếu mainFeatures");
        }
// target này ko bt làm fild gì nên chưa làm
//        if (geminiJobPostResponse.getTargetUsers() == null || geminiJobPostResponse.getTargetUsers().isBlank()) {
//            throw new GlobalException("AI trả thiếu targetUsers");
//        }


        if (geminiJobPostResponse.getRequiredSkills() == null || geminiJobPostResponse.getRequiredSkills().isBlank()) {
            throw new GlobalException("AI trả thiếu requiredSkills");
        }

        if (geminiJobPostResponse.getBudgets() == null) {
            throw new GlobalException("AI trả thiếu budgets");
        }

        if (geminiJobPostResponse.getTimeLine() == null || geminiJobPostResponse.getTimeLine().isBlank()) {
            throw new GlobalException("AI trả thiếu timeLine");
        }
    }
}
