package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.ExpertApplicationRequest;
import com.example.AiTaster.dto.response.ExpertApplicationResponse;

import java.util.List;

public interface IExpertApplication {
    ExpertApplicationResponse applyJobPost(Long jobPostId, ExpertApplicationRequest request);

// Xem chi tiết.
    List<ExpertApplicationResponse> getApplicationsByJobPost(Long jobPostId);
    // Expert xem danh sách application của chính mình.
     List<ExpertApplicationResponse> getMyApplications();

    ExpertApplicationResponse getApplicationDetail(Long applicationId);

    ExpertApplicationResponse unlockProposal(Long proposalId);

    }
