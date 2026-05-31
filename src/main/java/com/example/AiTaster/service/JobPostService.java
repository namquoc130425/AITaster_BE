package com.example.AiTaster.service;

import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.JobPostRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobPostService {
    private final JobPostRepo jobPostRepo;
    private final JobPostMapper jobPostMapper;
    private final ClientProfileRepo clientProfileRepo;
    private CurrentUserService currentUserService;
     //client tự tạo jobpost với status là Draft mà không dùng AI

    // lấy chi tiết của 1 job post


    //lấy danh sách job của client hiện tại


    //danh sách Jobpost status Public

    // Update Jobpost ( update lúc dang status Draft

    // Public JobPost -> đổi status Opends


    public JobPost findById(Long jobPostId) {
        return jobPostRepo.findById(jobPostId).orElseThrow(() -> new GlobalException("Không tìm thấy job post với id: " + jobPostId));
    }

    public ClientProfile getCurrentUser() {
        User currentUser = currentUserService.getCurrentUser();
        return clientProfileRepo.findByUser(currentUser).orElseThrow(() -> new GlobalException("User not found"));
    }
    // hàm check jobPost thuộc về client Profile nào
    private void checkJobPostByClientId(JobPost jobPost,ClientProfile clientProfile) {
        if(!clientProfile.getClientProfileId().equals(jobPost.getClientProfile().getClientProfileId())) {
            throw new GlobalException("Bạn không có quyền thao tác job post này");
        }
    }
}
