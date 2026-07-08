package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.JobPostRequest;
import com.example.AiTaster.dto.response.JobPostResponse;

import java.util.List;

public interface IJobPost {
    JobPostResponse createJobPost(JobPostRequest jobPostRequest);

    JobPostResponse UpdateJobPost(Long id,JobPostRequest jobPostRequest);



    JobPostResponse GetJobPostById(Long id);

    List<JobPostResponse> GetMyJobPostByClient ();

    List<JobPostResponse> GetAllJobPostPublic();

    void  DeleteJobPost(Long id);

    JobPostResponse hideJobPost(Long id);
}
