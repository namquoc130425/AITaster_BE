package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.request.JobPostRequest;

import com.example.AiTaster.dto.response.Ai.GeminiJobPostResponse;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostAiService {
    private final GeminiClientService geminiClientService;
    private final CurrentUserService currentUserService;
    private final JobPostRepo jobPostRepo;
    private final JobPostMapper jobPostMapper;
    private final ContentManagerService contentManagerService;
    private final ClientProfileRepo clientProfileRepo;
   // private final AiSearchSkillService aiSearchSkillService;


    public JobPostResponse CreatJobPostByAi(JobPostRequest request) throws JsonProcessingException {
        User userCurrent = currentUserService.getCurrentUser();
        // 2.  từ user tìm client profile
        ClientProfile clientProfile = clientProfileRepo.findByUser(userCurrent).orElseThrow(() -> new GlobalException("ClientProflie not found"));
        // check validate nguời dùng trước khi gữi Ai

       // contentManagerService.validateKeywordInput(jobPostAiRequest.getKeyword());
        //Ai tạo keywork tìm skill
       // AiSearchSkilResponse aiSearchSkilResponse = geminiClientService.searchSkillFromAi(request);
        //hệ thống querry từ key work mà ai trả ra
      //  List<AiSkillResult> aiSkillResultList = aiSearchSkillService.searchSkillByKeyword(aiSearchSkilResponse);
        // AI format JobPost với skill từ Db trả ra
        //GeminiJobPostResponse geminiJobPostResponse = geminiClientService.generateJobPost(request, aiSkillResultList);


        //valid dữ liệu của người dùng trước khi gữi cho AI

        // Lọc lại skill để đảm bảo AI chọn đúng skill DB

       // JobPost jobPostDraft = jobPostMapper.toEntityJobPostDraft(geminiJobPostResponse, clientProfile);
       // JobPost savedJobPost = jobPostRepo.save(jobPostDraft);

        return null;
    }

    // thêm hàm kiểm tra dử liệu trước khi gữi cho AI





    private void validateClientInput(JobPostRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new GlobalException("Client nhập thiếu title");
        }

        if (request.getMainFeatures() == null || request.getMainFeatures().isBlank()) {
            throw new GlobalException("Client nhập thiếu mainFeatures");
        }
        if (request.getBusinessGoal() == null || request.getBusinessGoal().isBlank()) {
            throw new GlobalException("Client nhập thiếu businessGoal");
        }
        if (request.getBudgets() == null) {
            throw new GlobalException("Client nhập thiếu budgets");
        }
        if (request.getTimeLine() == null || request.getTimeLine().isBlank()) {
            throw new GlobalException("Client nhập thiếu timeLine");

        }
        if(request.getRequirementDescription() == null || request.getRequirementDescription().isBlank()){
            throw new GlobalException("Client nhập thiếu requirementDescription");
        }


    }

// kiểm tra dữ liệu người dùng có từ cấm không
    private void checkValidateBlockedRequest(JobPostRequest request) {
        contentManagerService.validateKeywordInput(request.getTitle());
        contentManagerService.validateKeywordInput(request.getMainFeatures());
        contentManagerService.validateKeywordInput(request.getBusinessGoal());
        contentManagerService.validateKeywordInput(request.getRequirementDescription());
        contentManagerService.validateKeywordInput(request.getTimeLine());

    }



// dữ liệu ai trả ra với keyworks



  // dữ liệu Ai trả ra đầy đủ
    private void validateAiResponse(GeminiJobPostResponse geminiJobPostResponse) {
        if (geminiJobPostResponse == null) {
            throw new GlobalException("AI không tạo được dữ liệu job post");
        }

        if (geminiJobPostResponse.getTitle() == null || geminiJobPostResponse.getTitle().isBlank()) {
            throw new GlobalException("AI trả thiếu title");
        }

        if (geminiJobPostResponse.getRequirementDescription() == null || geminiJobPostResponse.getRequirementDescription().isBlank()) {
            throw new GlobalException("AI trả thiếu requirementDescription");
        }

        if (geminiJobPostResponse.getBusinessGoal() == null || geminiJobPostResponse.getBusinessGoal().isBlank()) {
            throw new GlobalException("AI trả thiếu businessGoal");
        }

        if (geminiJobPostResponse.getMainFeatures() == null || geminiJobPostResponse.getMainFeatures().isBlank()) {
            throw new GlobalException("AI trả thiếu mainFeatures");
        }


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
