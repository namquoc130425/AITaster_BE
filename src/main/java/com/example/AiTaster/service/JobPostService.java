package com.example.AiTaster.service;

import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.dto.request.JobPostAiRequest;
import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.Ai.GeminiJobPostResponse;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.Skill;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.service.imp.IJobPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostService implements IJobPost {
    private final JobPostRepo jobPostRepo;
    private final JobPostMapper jobPostMapper;
    private final ClientProfileRepo clientProfileRepo;
    private final CurrentUserService currentUserService;
    private final ContentManagerService  contentManagerService;
    private final SkillRepo skillRepo;

    //client tự tạo jobpost với status là Draft mà không dùng AI
    public JobPostResponse createJobPost(JobPostRequest jobPostRequest) {
        validateUserInputContent(jobPostRequest);
        ClientProfile clientProfileByUser = getCurrentClientProfile();
        List<Skill> selectedSkillByUser = getSkillBySkillId(jobPostRequest.getSelectedSkillIds());
        JobPost jobPost = jobPostMapper.toEntityJobPostDraft(jobPostRequest, clientProfileByUser);
               jobPost.setSkills(selectedSkillByUser);
        JobPost saveJobPost = jobPostRepo.save(jobPost);

        return  jobPostMapper.toResponse(saveJobPost);
    }

    // Update Jobpost ( update lúc dang status Draft )
    @Override
    public JobPostResponse UpdateJobPost(Long id, JobPostRequest jobPostRequest) {
        validateUserInputContent(jobPostRequest);

        ClientProfile clientProfile = getCurrentClientProfile();
        JobPost jobPost = findJobPostById(id);

        checkJobPostByClientId(jobPost, clientProfile);

        jobPostMapper.updateEntity(jobPostRequest, jobPost);
        List<Skill> selectedSkills = getSkillBySkillId(jobPostRequest.getSelectedSkillIds());
        jobPost.setSkills(selectedSkills);
        JobPost saveJobPost = jobPostRepo.save(jobPost);
        return jobPostMapper.toResponse(saveJobPost);

    }

    // lấy chi tiết của 1 job post
    @Override
    public JobPostResponse GetJobPostById(Long id) {
        JobPost jobPost = findJobPostById(id);
        return jobPostMapper.toResponse(jobPost);
    }

    //lấy danh sách job của client mới nhất . ( đẩy dử liệu lên để cho người dùng chỉnh sửa )
    @Override
    public List<JobPostResponse> GetMyJobPostByClient() {
        ClientProfile clientProfile = getCurrentClientProfile();
        return jobPostRepo.findByClientProfileOrderByCreateAtDesc(clientProfile).stream().map(jobPostMapper::toResponse).toList();
    }

    //lấy danh sách job của client có status Opend hiện tại
    @Override
    public List<JobPostResponse> GetAllJobPostPublic() {
        return jobPostRepo.findByJobPostStatus(JobpostStatus.OPEN).stream().map(jobPostMapper::toResponse).toList();
    }


    @Override
    public void DeleteJobPost(Long id) {
        ClientProfile clientProfile = getCurrentClientProfile();

        JobPost jobPost = findJobPostById(id);

        checkJobPostByClientId(jobPost, clientProfile);

        jobPostRepo.delete(jobPost);


    }

    // Public JobPost -> đổi status Opends
    @Override
    public JobPostResponse publishJobPost(Long id) {

        ClientProfile clientProfile = getCurrentClientProfile();

        JobPost jobPost = findJobPostById(id);

        checkJobPostByClientId(jobPost, clientProfile);

        if (!jobPost.getJobPostStatus().equals(JobpostStatus.DRAFT)) {
            throw new GlobalException("Job Post Status Not Found");
        }

        jobPost.setJobPostStatus(JobpostStatus.OPEN);

        JobPost saveJobPost = jobPostRepo.save(jobPost);

        return jobPostMapper.toResponse(saveJobPost);
    }


    public JobPost findJobPostById(Long jobPostId) {
        return jobPostRepo.findJobPostByjobPostId(jobPostId).orElseThrow(() -> new GlobalException("Không tìm thấy job post với id: " + jobPostId));
    }

    // lấy profile của user đang nhập hiện tại mà ko cần front end truyền id vào
    public ClientProfile getCurrentClientProfile() {
        User currentUser = currentUserService.getCurrentUser();
        return clientProfileRepo.findByUser(currentUser).orElseThrow(() -> new GlobalException("Client Profile Not Found"));
    }

    // hàm check jobPost thuộc về client Profile nào
    private void checkJobPostByClientId(JobPost jobPost, ClientProfile clientProfile) {
        if (!clientProfile.getClientProfileId().equals(jobPost.getClientProfile().getClientProfileId())) {
            throw new GlobalException("Bạn không có quyền thao tác job post này");
        }
    }

    // Check nội dung user nhập có từ cấm / prompt injection không
    private void validateUserInputContent(JobPostRequest request) {

        // Nếu request null thì chặn trước
        if (request == null) {
            throw new GlobalException(400, "Request is required");
        }

        // Check title
        contentManagerService.validateKeywordInput(request.getTitle());

        // Check mô tả yêu cầu
        contentManagerService.validateKeywordInput(request.getRequirementDescription());

        // Check mục tiêu kinh doanh
        contentManagerService.validateKeywordInput(request.getBusinessGoal());

        // Check chức năng chính
        contentManagerService.validateKeywordInput(request.getMainFeatures());

        // Check timeline
        contentManagerService.validateKeywordInput(request.getTimeLine());
    }

    private List<Skill> getSkillBySkillId(List<Long> selectedSkillIds) {

        if (selectedSkillIds == null || selectedSkillIds.isEmpty()) {
            return List.of();
        }

        // KIỂM TRA TỪNG SKILLID , SKILLNAME V,,V CÓ BỊ NULL , CÓ BỊ TRÙNG HAY KHÔNG
        List<Long> checkskill = new ArrayList<>();

        //duyệt từng id trong danh sách user gữi lên
        for (Long skillId : selectedSkillIds) {
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


}
