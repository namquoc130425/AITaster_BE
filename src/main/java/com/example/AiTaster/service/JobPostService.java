package com.example.AiTaster.service;

import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.service.imp.IJobPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostService implements IJobPost {
    private final JobPostRepo jobPostRepo;
    private final JobPostMapper jobPostMapper;
    private final ClientProfileRepo clientProfileRepo;
    private final CurrentUserService currentUserService;

     //client tự tạo jobpost với status là Draft mà không dùng AI
    public JobPostResponse createJobPost(JobPostRequest jobPostRequest) {

     //User currentUser = currentUserService.getCurrentUser();
    //  ClientProfile clientProfileId = clientProfileRepo.findByUser(currentUser).orElseThrow(() -> new GlobalException("User not found"));
        ClientProfile clientProfileByUser = getCurrentClientProfile();
        JobPost JobPost = jobPostMapper.toEntityJobPostDraft(jobPostRequest,clientProfileByUser);
       JobPost saveJobPost = jobPostRepo.save(JobPost);

        return jobPostMapper.toResponse(saveJobPost);
    }
    // Update Jobpost ( update lúc dang status Draft )
    @Override
    public JobPostResponse UpdateJobPost(Long id, JobPostRequest jobPostRequest) {
        JobPost jobPost = jobPostRepo.findById(id).orElseThrow(() -> new GlobalException("Job Post Not Found"));

        jobPostMapper.updateEntity(jobPostRequest,jobPost);
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
        return jobPostRepo.findByClientProfileOrderByCreateAtDesc(clientProfile).stream().map(jobPostMapper :: toResponse).toList();
    }
    //lấy danh sách job của client có status Opend hiện tại
    @Override
    public List<JobPostResponse> GetAllJobPostPublic() {
        return jobPostRepo.findByJobPostStatus(JobpostStatus.OPEN).stream().map(jobPostMapper :: toResponse).toList();
    }


    @Override
    public void DeleteJobPost(Long id) {
        JobPost jobPost = jobPostRepo.findById(id).orElseThrow(() -> new GlobalException("Job Post Not Found"));
        jobPostRepo.delete(jobPost);


    }

    // Public JobPost -> đổi status Opends
    @Override
    public JobPostResponse publishJobPost(Long id) {

      ClientProfile clientProfile = getCurrentClientProfile();

      JobPost jobPost = findJobPostById(id);

      checkJobPostByClientId(jobPost,clientProfile);

if(!jobPost.getJobPostStatus().equals(JobpostStatus.DRAFT)) {
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
    private void checkJobPostByClientId(JobPost jobPost,ClientProfile clientProfile) {
        if(!clientProfile.getClientProfileId().equals(jobPost.getClientProfile().getClientProfileId())) {
            throw new GlobalException("Bạn không có quyền thao tác job post này");
        }
    }
}
