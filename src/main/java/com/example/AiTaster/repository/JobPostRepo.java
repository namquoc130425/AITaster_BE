package com.example.AiTaster.repository;

import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface JobPostRepo extends JpaRepository<JobPost, Long> {
   Optional<JobPost>  findJobPostByjobPostId(Long jobPostId);
   List<JobPost> findByClientProfileOrderByCreateAtDesc(ClientProfile clientProfile); // Lấy job của client, mới nhất trước

   List<JobPost> findByJobPostStatus(JobpostStatus jobPostStatus); // Lấy tất cả job theo status Opends
}

