package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.request.JobPostRequest;

import com.example.AiTaster.dto.response.Ai.GeminiJobPostResponse;
import com.example.AiTaster.dto.response.Ai.VectorSkillResult;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.Skill;

import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.SkillRepo;

import com.example.AiTaster.service.vector.SkillVectorSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobPostAiService {
    private final GeminiClientService geminiClientService;
    private final CurrentUserService currentUserService;
    private final JobPostMapper jobPostMapper;
    private final ContentManagerService contentManagerService;
    private final ClientProfileRepo clientProfileRepo;
    private final SkillRepo skillRepo;
    private final SkillVectorSearchService skillVectorSearchService;

    public JobPostResponse creatJobPostByAi(JobPostAiRequest request) {

        validateUserInputContent(request);

        User currentUser = currentUserService.getCurrentUser();

        ClientProfile clientProfile = clientProfileRepo.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new GlobalException(403, "Chỉ khách hàng mới có thể tạo tin tuyển dụng"));
        //lấy selected từ fe
        List<Skill> selectedSkills = getSelectedSkills(request.getSelectedSkillIds());
        //build search text
        String buildText = buildJobPostText(request, selectedSkills);
        //Search skill candidate trong Qdrant và yêu cầu lấy 10 đứa gần nghĩa nhất
        List<VectorSkillResult> vectorResults = skillVectorSearchService.searchSkillResult(buildText, 10);
        //Nếu Qdrant không trả skill nào thì báo lỗi.
        if (vectorResults == null || vectorResults.isEmpty()) {
            throw new GlobalException(400, "Không tìm thấy kỹ năng phù hợp từ Qdrant");
        }
        // Giới hạn số số skill mà qdrant  đưa cho Gemini.
        List<VectorSkillResult> aiLimitResult = limitResult(vectorResults, 8);

        //Gọi Gemini.

        GeminiJobPostResponse aiResponse = geminiClientService.generateJobPost(request, aiLimitResult);

        validateAiResponse(aiResponse);

        //Validate finalSkillIds do Gemini trả về có nằm trong vectorResults

        List<Long> finalSkillIds = validateAiFinalSkillIds(aiResponse.getFinalSkillIds(), aiLimitResult);

        //Nếu AI không trả skill hợp lệ thì báo lỗi.

        if (finalSkillIds.isEmpty()) {
            throw new GlobalException(400, "AI không trả về kỹ năng hợp lệ");
        }

        //Lấy Skill entity thật từ MySQL.
        List<Skill> skillToDb = getSkillBySkillId(finalSkillIds);

        JobPost jobPost = jobPostMapper.toEntityJobPostDraft(aiResponse, clientProfile);
        jobPost.setSkills(skillToDb);

        return jobPostMapper.toResponse(jobPost);
    }


    // Hàm lấy skill user chọn từ dữ liệu yêu cầu.
    // FE trả id về thì mình dùng id đó xuống DB lấy list skill.
    private List<Skill> getSelectedSkills(List<Long> selectedSkillIds) {
        // Kiểm tra client có nhập không, nếu không nhập thì trả về list rỗng.
        if (selectedSkillIds == null || selectedSkillIds.isEmpty()) {
            return List.of();
        }
        List<Long> validIds = new ArrayList<>();


        //Duyệt từng skillId FE gửi lên.

        for (Long skillId : selectedSkillIds) {
            if (skillId == null) {
                continue;
            }
            // Trùng thì bỏ qua.
            if (!validIds.contains(skillId)) {
                validIds.add(skillId);
            }
        }

        if (validIds.isEmpty()) {
            return List.of();
        }


        List<Skill> skills = skillRepo.findAllById(validIds);

        // Nếu số skill lấy ra khác số ID hợp lệ,
        // nghĩa là có ID không tồn tại trong database.

        if (skills.size() != validIds.size()) {
            throw new GlobalException(400, "Một số kỹ năng đã chọn không tồn tại");
        }

        return skills;

    }

    // Hàm này gom text của JobPostAiRequest để tìm kiếm Qdrant.
    // Biến dữ liệu job post thành một đoạn text.
    private String buildJobPostText(JobPostAiRequest jobPostAiRequest, List<Skill> selectedSkills) {
        String textSelectedSkills = buildSelectedSkillText(selectedSkills);

        return """
                Job title: %s
                Requirement description: %s
                Business goal: %s
                Main features: %s
                Budget: %s
                Timeline: %s
                User selected skills: %s
                """.formatted(
                jobPostAiRequest.getTitle(),
                jobPostAiRequest.getRequirementDescription(),
                jobPostAiRequest.getBusinessGoal(),
                jobPostAiRequest.getMainFeatures(),
                jobPostAiRequest.getBudgets(),
                jobPostAiRequest.getTimeLine(),
                textSelectedSkills
        );
    }

    // Hàm này biến List<Skill> user chọn thành một chuỗi text.
    private String buildSelectedSkillText(List<Skill> selectedSkills) {
        if (selectedSkills == null || selectedSkills.isEmpty()) {
            return "";
        }

        // StringBuilder nối chuỗi hiệu quả hơn.
        StringBuilder builder = new StringBuilder();

        // Duyệt từng Skill user chọn.
        for (Skill skill : selectedSkills) {

            if (skill == null) {
                continue;
            }
            if (skill.getSkillName() == null || skill.getSkillName().isBlank()) {
                continue;
            }


            // Nếu builder đã có dữ liệu trước đó,
            // thêm dấu ", " để ngăn cách các skill name.
            if (builder.length() > 0) {
                builder.append(", ");
            }
            // Thêm tên skill vào chuỗi kết quả.
            builder.append(skill.getSkillName());
        }

        return builder.toString();
    }


    // Hàm này giới hạn số lượng skill đưa cho AI.
    // Qdrant có thể trả nhiều skill nên cần giới hạn lại.
    private List<VectorSkillResult> limitResult(List<VectorSkillResult> vectorSkillResult, int limit) {

        // result là danh sách candidate cuối cùng đưa cho AI.
        List<VectorSkillResult> result = new ArrayList<>();

        // Nếu vectorSkillResult null hoặc rỗng thì trả list rỗng.
        if (vectorSkillResult == null || vectorSkillResult.isEmpty()) {
            return result;
        }

        //  Duyệt từng result Qdrant trả về.
        for (VectorSkillResult candidate : vectorSkillResult) {

            //  Nếu candidate null thì bỏ qua.
            if (candidate == null) {
                continue;
            }
            // Add candidate vào result.
            result.add(candidate);
            //Nếu result =  limit thì dừng vòng lặp.
            if (result.size() == limit) {
                break;
            }
        }

        // Trả về danh sách candidate đã giới hạn.
        return result;

    }


    //validateFinalSkillIds
    //hàm kiểm tra xem Ai trả về skillIds có hợp lệ không
    //Ai chỉ đc phép trả skill trong vectorSkillResult
    private List<Long> validateAiFinalSkillIds(List<Long> aiSkillIds, List<VectorSkillResult> vectorSkillResult) {
        if (aiSkillIds == null || aiSkillIds.isEmpty()) {
            return List.of();
        }
        // vectorResult là danh sách Ai được phép chọn

        Set<Long> allowedSkillIds = new HashSet<>();
        for (VectorSkillResult result : vectorSkillResult) {
            if (result == null) {
                continue;
            }
            if (result.getSkillId() == null) {
                continue;
            }
            allowedSkillIds.add(result.getSkillId());

        }
        //ValidSkills danh sách Id hợp lệ sau khi valid
        List<Long> validSkillIds = new ArrayList<>();
        for (Long aiSkillResult : aiSkillIds) {
            if (aiSkillResult == null) {
                continue;
            }

            //Nếu AI trả ID không nằm trong allowedSkillIds thì bỏ qua.
            if (!allowedSkillIds.contains(aiSkillResult)) {
                continue;
            }

            // Nếu ID này chưa có trong validSkillIds thì mới add.
            // Mục đích: bỏ ID trùng.
            if (!validSkillIds.contains(aiSkillResult)) {
                validSkillIds.add(aiSkillResult);
            }
        }

        return validSkillIds;
    }


    //hàm này lấy skill entity thật từ sql  theo danh sách skill id
    //Ai trả ra Id skill nên mình cầm id đó xuong db tạo thành list<skil> trả ra cho Fe
    private List<Skill> getSkillBySkillId(List<Long> skillIds) {

        if (skillIds == null || skillIds.isEmpty()) {
            return List.of();
        }

        // KIỂM TRA TỪNG SKILLID , SKILLNAME V,,V CÓ BỊ NULL , CÓ BỊ TRÙNG HAY KHÔNG
        List<Long> checkskill = new ArrayList<>();

        //duyệt từng id trong danh sách user gữi lên
        for (Long skillId : skillIds) {
            // null bỏ qua ko add
            if (skillId == null) {
                continue;
            }
            if(skillId <= 0) {
                continue;
            }
            //  Nếu skillId chưa có thì add vào validIds. nếu có rồi thì bỏ qua
            if (!checkskill.contains(skillId)) {
                checkskill.add(skillId);
            }

        }
        if (checkskill.isEmpty()) {
            return List.of();
        }

        return skillRepo.findAllById(checkskill);
    }

    // Kiểm tra nội dung user nhập có từ cấm hoặc prompt injection không.
    private void validateUserInputContent(JobPostAiRequest request) {

        // Nếu dữ liệu yêu cầu null thì chặn trước.
        if (request == null) {
            throw new GlobalException(400, "Yêu cầu là bắt buộc");
        }

        // Kiểm tra title.
        contentManagerService.validateKeywordInput(request.getTitle());

        // Kiểm tra mô tả yêu cầu.
        contentManagerService.validateKeywordInput(request.getRequirementDescription());

        // Check mục tiêu kinh doanh
        contentManagerService.validateKeywordInput(request.getBusinessGoal());

        // Check chức năng chính
        contentManagerService.validateKeywordInput(request.getMainFeatures());

        // Check timeline
        contentManagerService.validateKeywordInput(request.getTimeLine());
    }

    // Validate dữ liệu Gemini trả về
    private void validateAiResponse(GeminiJobPostResponse aiResponse) {

        if (aiResponse == null) {
            throw new GlobalException(500, "Phản hồi AI đang trống");
        }

        contentManagerService.validateKeywordInput(aiResponse.getTitle());

        contentManagerService.validateKeywordInput(
                aiResponse.getRequirementDescription()
        );

        contentManagerService.validateKeywordInput(
                aiResponse.getBusinessGoal()
        );

        contentManagerService.validateKeywordInput(
                aiResponse.getMainFeatures()
        );

        contentManagerService.validateKeywordInput(
                aiResponse.getTimeLine()
        );

        if (aiResponse.getFinalSkillIds() == null || aiResponse.getFinalSkillIds().isEmpty()) {
            throw new GlobalException(500, "Danh sách mã kỹ năng cuối cùng từ AI đang trống");
        }
    }

}
