package com.example.AiTaster.service;

import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.dto.request.ExpertProposalRequest;
import com.example.AiTaster.dto.response.ExpertProposalPreviewResponse;
import com.example.AiTaster.dto.response.ExpertProposalResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertProposalMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.imp.IExpertProposal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpertProposalService implements IExpertProposal {
    private final ExpertProposalRepo  expertProposalRepo;
    private final ProposalUnlockRepo proposalUnlockRepo;
    private final JobPostRepo jobPostRepo;
    private final ExpertProfileRepo expertProfileRepo;
    private final ClientProfileRepo clientProfileRepo;
    private final CurrentUserService currentUserService;
    private final ContentManagerService contentManagerService;
    private final ExpertProposalMapper expertProposalMapper;


    @Override
    public ExpertProposalResponse createProposal(Long jobPostId, ExpertProposalRequest request) {
        validateInputContent(request);
        ExpertProfile expertProfile = getCurrentExpertProfile();
        JobPost jobPost = getJobPostById(jobPostId);
        if(!jobPost.getJobPostStatus().equals(JobpostStatus.OPEN)) {
            throw new GlobalException("Job post is not OPEN");
        }
        boolean existed = expertProposalRepo.existsByJobpostAndExpertProfileAndIsDeletedFalse(jobPost,expertProfile);
        //true là có rồi thì báo lỗi , chưa có thì thêm
        if(existed) {
            throw new GlobalException(400, "You already created proposal for this jobPost");
        }
        ExpertProposal expertProposal = expertProposalMapper.toEntity(request,jobPost,expertProfile);
        ExpertProposal savedProposal = expertProposalRepo.save(expertProposal);

        //hàm này trả ra cả detailcontent vì nó là do expert tạo và đã mở khóa để expert có thể coi
        //còn client nếu chưa mua mà xem thì sẽ trã DetailContent là null và chưa mở khóa
        return expertProposalMapper.toResponse(expertProposal,savedProposal.getDetailContent(),true);
    }

    @Override
    public List<ExpertProposalPreviewResponse> getProposalsByJobPost(Long jobPostId) {
        ClientProfile clientProfile = getCurrentClientProfile();
        JobPost jobPost = getJobPostById(jobPostId);
        checkJobPostOwner(jobPost,clientProfile);
        // từ jobpost lay ra cac proposals chưa bị xóa mềm
        List<ExpertProposal> proposals = expertProposalRepo.findByJobpostAndIsDeletedFalseOrderByCreateAtDesc(jobPost);

        // Tạo list response rỗng.
        List<ExpertProposalPreviewResponse> responses = new ArrayList<>();
        for(ExpertProposal expertProposal : proposals) {
            Boolean isUnlocked = proposalUnlockRepo.existsByProposalAndClientProfileAndIsUnlockedTrue(expertProposal,clientProfile);

            ExpertProposalPreviewResponse expertProposalPreviewResponse = expertProposalMapper.toPreviewResponse(expertProposal,isUnlocked);
            responses.add(expertProposalPreviewResponse);
        }
        return responses;
    }

    @Override
    public List<ExpertProposalPreviewResponse> getMyProposals() {
        ExpertProfile expertProfile = getCurrentExpertProfile();
        // Lấy list proposal của expert mà chưa xóa mềm và sắp xếp mới nhất.
        List<ExpertProposal> proposals = expertProposalRepo.findByExpertProfileAndIsDeletedFalseOrderByCreateAtDesc(expertProfile);
        List<ExpertProposalPreviewResponse> responses = new ArrayList<>();
        for (ExpertProposal proposal : proposals) {
            // Expert owner xem proposal của mình, set isUnlocked = true cho FE dễ hiểu.
            ExpertProposalPreviewResponse response = expertProposalMapper.toPreviewResponse(proposal, true);
            responses.add(response);
        }


        return responses;
    }

    @Override
    public ExpertProposalResponse getProposalDetail(Long proposalId) {
        ExpertProposal expertProposal = getProposalById(proposalId);
        // Nếu proposal đã xóa mềm thì không cho xem.
        if (Boolean.TRUE.equals(expertProposal.getIsDeleted())) {
            throw new GlobalException(400, "Proposal was deleted");
        }
        if(isCurrentExpertOwner(expertProposal)){
           return  expertProposalMapper.toResponse(expertProposal,expertProposal.getDetailContent(),true);
        }

        ClientProfile clientProfile = getCurrentClientProfile();
        // Check client có phải chủ jobpost không.
        checkJobPostOwner(expertProposal.getJobpost(),clientProfile);

        // Check client đã unlock proposal chưa.
        boolean isUnlocked = proposalUnlockRepo.existsByProposalAndClientProfileAndIsUnlockedTrue(expertProposal, clientProfile);
        // Mặc định không trả detailContent.
        String detailContent = null;

        // Nếu đã unlock thì mới trả detailContent.
        if (isUnlocked) {
            detailContent = expertProposal.getDetailContent();
        }

        return expertProposalMapper.toResponse(expertProposal, detailContent, isUnlocked);
    }

    @Override
    public ExpertProposalResponse updateProposal(Long proposalId, ExpertProposalRequest request) {
        validateInputContent(request);
        ExpertProfile expertProfile = getCurrentExpertProfile();
      ExpertProposal expertProposal = getProposalById(proposalId);
        checkProposalOwner(expertProposal,expertProfile);

        if (Boolean.TRUE.equals(expertProposal.getIsDeleted())) {
            throw new GlobalException(400, "Proposal was deleted");
        }
         expertProposalMapper.UpdateEntiy(request,expertProposal);
        ExpertProposal savedProposal = expertProposalRepo.save(expertProposal);

        return expertProposalMapper.toResponse(savedProposal,savedProposal.getDetailContent(),true);
    }

    @Override
    public void deleteProposal(Long proposalId) {
        ExpertProfile expertProfile = getCurrentExpertProfile();
        ExpertProposal proposal = getProposalById(proposalId);
        checkProposalOwner(proposal, expertProfile);
        proposal.setIsDeleted(true);
        expertProposalRepo.save(proposal);
    }

    @Override
    public ExpertProposalResponse unlockProposal(Long proposalId) {
        ClientProfile clientProfile = getCurrentClientProfile();
        ExpertProposal proposal = getProposalById(proposalId);
        // Không cho unlock proposal đã xóa mềm.
        if (Boolean.TRUE.equals(proposal.getIsDeleted())) {
            throw new GlobalException(400, "Proposal was deleted");
        }
        checkJobPostOwner(proposal.getJobpost(),clientProfile);

        Boolean checkClientUnlocked = proposalUnlockRepo.existsByProposalAndClientProfileAndIsUnlockedTrue(proposal,clientProfile);
        if(!checkClientUnlocked){
            ProposalUnlock unlock = ProposalUnlock.builder()
                    .proposal(proposal)
                    .clientProfile(clientProfile)
                    .paymentTransactionId(null)
                    .amount(proposal.getPriceToUnlock())
                    .isUnlocked(true)
                    .unlockedAt(LocalDateTime.now())
                    .build();
            proposalUnlockRepo.save(unlock);
        }

        return expertProposalMapper.toResponse(proposal,proposal.getDetailContent(),true);
    }


    //từ id tìm Jobpost
    private JobPost getJobPostById(Long jobPostId) {
        return  jobPostRepo.findJobPostByjobPostId(jobPostId).orElseThrow(() ->  new GlobalException("JobPost not found"));
    }

    //tìm Proposal theo ProposalId
    private ExpertProposal getProposalById(Long proposalId) {
        return expertProposalRepo.findExpertProposalByProposalId(proposalId).orElseThrow(() ->  new GlobalException("Proposal not found"));
    }

    //lấy expertProfile từ User đang đăng nhập
    private ExpertProfile getCurrentExpertProfile() {
        User user = currentUserService.getCurrentUser();
        return expertProfileRepo.findByUser(user).orElseThrow(() -> new GlobalException(403, "Only expert can use this API"));
    }

    //lấy CilentProfile từ User đang đăng nhập
    private ClientProfile getCurrentClientProfile() {
        User user = currentUserService.getCurrentUser();
        return clientProfileRepo.findByUser(user).orElseThrow(() -> new GlobalException(403, "Only client can use this API"));
    }

    //check expert có phải là chủ của Probosal ko
    private void checkProposalOwner (ExpertProposal expertProposal,ExpertProfile expertProfile) {
       if(!expertProposal.getExpertProfile().getExpertProfileId().equals(expertProfile.getExpertProfileId())) {
           throw new  GlobalException(403, "You are not owner of this proposal");
       }
    }
// check client có là chủ nhận của cái JobPost đó ko =)))
    private void checkJobPostOwner(JobPost jobPost ,ClientProfile clientProfile) {
        if(!jobPost.getClientProfile().getClientProfileId().equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not owner of this jobpost");
        }
    }




    private void validateInputContent(ExpertProposalRequest request) {
        if (request == null) {
            throw new GlobalException(400, "Request is required");
        }

        contentManagerService.validateKeywordInput(request.getTitle());
        contentManagerService.validateKeywordInput(request.getSummary());
        if (request.getTechnologies() != null) {
            contentManagerService.validateKeywordInput(request.getTechnologies());
        }
        contentManagerService.validateKeywordInput(request.getDetailContent());
    }
// cũng check tk expert có sở hữu proposal ko nhưng trả về true và false
    //nếu nó sở hữu thì nó đc xem detail
    private boolean isCurrentExpertOwner(ExpertProposal proposal){

        User user = currentUserService.getCurrentUser();

        Optional<ExpertProfile> optionalExpertProfile = expertProfileRepo.findByUser(user);
        if (optionalExpertProfile.isEmpty()) {
            return false;
        }
        ExpertProfile expertProfile = optionalExpertProfile.get();

        if (proposal.getExpertProfile().getExpertProfileId()
                .equals(expertProfile.getExpertProfileId())) {
            return true;
        }

        return false;

    }

}
