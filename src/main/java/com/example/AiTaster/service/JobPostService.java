package com.example.AiTaster.service;

import com.example.AiTaster.Util.PageUtil;
import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.dto.request.JobPost.JobPostFilterRequest;
import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.Skill;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertApplicationRepo;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.service.imp.IJobPost;
import com.example.AiTaster.specification.JobPostSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ExpertApplicationRepo expertApplicationRepo;

    //client tự tạo jobpost với status là Draft mà không dùng AI
    public JobPostResponse createJobPost(JobPostRequest jobPostRequest) {
        validateUserInputContent(jobPostRequest);
        ClientProfile clientProfileByUser = getCurrentClientProfile();
        ensureNoDuplicateActiveJobPost(clientProfileByUser, jobPostRequest);
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
    @Transactional
    public List<JobPostResponse> GetMyJobPostByClient() {
        ClientProfile clientProfile = getCurrentClientProfile();
        jobPostRepo.closeOpenJobPostsWithAcceptedInvitation(
                clientProfile,
                JobpostStatus.OPEN,
                JobpostStatus.CLOSED,
                InvitationStatus.ACCEPTED
        );
        return jobPostRepo.findByClientProfileOrderByCreateAtDesc(clientProfile)
                .stream()
                .filter(jobPost -> jobPost.getJobPostStatus() != JobpostStatus.CANCELED)
                .map(this::toMyJobPostResponse)
                .toList();
    }

    //lấy danh sách job của client có status Opend hiện tại
    @Override
    public List<JobPostResponse> GetAllJobPostPublic() {
        return jobPostRepo.findPublicOpenJobPosts(
                JobpostStatus.OPEN,
                List.of(
                        InvitationStatus.PENDING,
                        InvitationStatus.ACCEPTED,
                        InvitationStatus.PAYMENT_EXPIRED
                )
        ).stream().map(jobPostMapper::toResponse).toList();
    }

    public PageResponse<JobPostResponse> getAllPublicJobPostsPage(JobPostFilterRequest jobPostFilterRequest) {
        Pageable pageable = PageUtil.createPageable(jobPostFilterRequest);
        Page<JobPost> jobPostPage = jobPostRepo.findAll(JobPostSpecification.filter(jobPostFilterRequest), pageable);
        Page<JobPostResponse> responsePage = jobPostPage.map(jobPostMapper::toResponse);

        return PageResponse.fromPage(responsePage);
    }

    private JobPostResponse toMyJobPostResponse(JobPost jobPost) {
        JobPostResponse response = jobPostMapper.toResponse(jobPost);
        response.setApplicationCount(expertApplicationRepo.countByJobpost(jobPost));
        return response;
    }


    @Override
    public void DeleteJobPost(Long id) {
        ClientProfile clientProfile = getCurrentClientProfile();

        JobPost jobPost = findJobPostById(id);

        checkJobPostByClientId(jobPost, clientProfile);

        jobPost.setJobPostStatus(JobpostStatus.CANCELED);
        jobPostRepo.save(jobPost);


    }

    // Public JobPost -> đổi status HIDDEN
    @Override
    public JobPostResponse hideJobPost(Long id) {

        ClientProfile clientProfile = getCurrentClientProfile();

        JobPost jobPost = findJobPostById(id);

        checkJobPostByClientId(jobPost, clientProfile);

        jobPost.setJobPostStatus(JobpostStatus.HIDDEN);

        JobPost saveJobPost = jobPostRepo.save(jobPost);

        return jobPostMapper.toResponse(saveJobPost);
    }


    public JobPostResponse changeJobPostStatus(Long id, JobpostStatus jobPostStatus) {
        if (jobPostStatus == null) {
            throw new GlobalException(400, "Job post status is required");
        }

        ClientProfile clientProfile = getCurrentClientProfile();

        JobPost jobPost = findJobPostById(id);

        checkJobPostByClientId(jobPost, clientProfile);

        jobPost.setJobPostStatus(jobPostStatus);

        JobPost savedJobPost = jobPostRepo.save(jobPost);

        return toMyJobPostResponse(savedJobPost);
    }

    public JobPost findJobPostById(Long jobPostId) {
        return jobPostRepo.findJobPostByjobPostId(jobPostId).orElseThrow(() -> new GlobalException("Không tìm thấy job post với id: " + jobPostId));
    }

    // lấy profile của user đang nhập hiện tại mà ko cần front end truyền id vào
    public ClientProfile getCurrentClientProfile() {
        User currentUser = currentUserService.getCurrentUser();
        return clientProfileRepo.findByUser(currentUser)
                .orElseThrow(() -> new GlobalException(403, "Only client can access job posts"));
    }

    // hàm check jobPost thuộc về client Profile nào
    private void checkJobPostByClientId(JobPost jobPost, ClientProfile clientProfile) {
        if (!clientProfile.getClientProfileId().equals(jobPost.getClientProfile().getClientProfileId())) {
            throw new GlobalException("Bạn không có quyền thao tác job post này");
        }
    }

    // Check nội dung user nhập có từ cấm / prompt injection không
    private void validateUserInputContent(JobPostRequest request) {

        // Nếu dữ liệu yêu cầu null thì chặn trước.
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

        if (request.getBudgets() == null || request.getBudgets().compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Budget must be greater than 0");
        }
    }

    private void ensureNoDuplicateActiveJobPost(ClientProfile clientProfile, JobPostRequest request) {
        boolean duplicated = jobPostRepo.existsDuplicateActiveJobPost(
                clientProfile,
                List.of(JobpostStatus.DRAFT, JobpostStatus.OPEN),
                normalizeText(request.getTitle()),
                normalizeText(request.getRequirementDescription()),
                normalizeText(request.getBusinessGoal()),
                normalizeText(request.getMainFeatures()),
                request.getBudgets(),
                normalizeText(request.getTimeLine())
        );

        if (duplicated) {
            throw new GlobalException(409, "Duplicate job post request. Please do not submit the same job post twice");
        }
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase();
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
