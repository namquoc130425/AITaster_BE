package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.ExpertProposalRequest;
import com.example.AiTaster.dto.response.ExpertProposalPreviewResponse;
import com.example.AiTaster.dto.response.ExpertProposalResponse;

import java.util.List;

public interface IExpertProposal {

    ExpertProposalResponse createProposal(Long jobPostId, ExpertProposalRequest request);


    // Trả về List proposal preview trong 1 JobPost
    List<ExpertProposalPreviewResponse> getProposalsByJobPost(Long jobPostId);

    //trả về List MyProposals của expert
    List<ExpertProposalPreviewResponse> getMyProposals();

    //Client chủ của Jobpost xem chi tiết 1 Proposals và mặc định là detailContent chưa mở và Iunlock == true thì mới cho xem detailContent
    ExpertProposalResponse getProposalDetail(Long proposalId);

    //Expert UpdateProposal của mình
    ExpertProposalResponse updateProposal(Long proposalId, ExpertProposalRequest request);

    void deleteProposal(Long proposalId);

    // mua để xem detailContent sau này bổ payment vào đây .
    ExpertProposalResponse unlockProposal(Long proposalId);

}
