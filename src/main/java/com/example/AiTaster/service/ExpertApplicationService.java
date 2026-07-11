package com.example.AiTaster.service;

import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.dto.request.ExpertApplicationRequest;
import com.example.AiTaster.dto.request.ExpertProposalRequest;
import com.example.AiTaster.dto.response.ExpertApplicationResponse;
import com.example.AiTaster.dto.response.ExpertProposalResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertApplicationMapper;
import com.example.AiTaster.mapper.ExpertProposalMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.imp.IExpertApplication;
import com.example.AiTaster.service.payment.ProposalPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;



@Service
@RequiredArgsConstructor
public class ExpertApplicationService implements IExpertApplication {
private final ExpertApplicationMapper expertApplicationMapper;
private final ExpertProposalMapper expertProposalMapper;
private final ExpertProposalRepo expertProposalRepo;
private final ExpertApplicationRepo  expertApplicationRepo;
private final ContentManagerService contentManagerService;
private final ExpertProfileRepo expertProfileRepo;
private final CurrentUserService currentUserService;
private final ClientProfileRepo clientProfileRepo;
private final ProposalUnlockRepo proposalUnlockRepo;
private final JobPostRepo jobPostRepo;
private final ProposalPurchaseService proposalPurchaseService;
private final NotificationService notificationService;
private final ExpertVerificationGuardService expertVerificationGuardService;

    @Override
    public ExpertApplicationResponse applyJobPost(Long jobPostId, ExpertApplicationRequest request) {
        validateApplicationInput(request);
        ExpertProfile expertProfile = getCurrentExpertProfile();
        ensureExpertVerified(expertProfile);
        JobPost jobPost = jobPostRepo.findJobPostByjobPostId(jobPostId)
                .orElseThrow(() -> new GlobalException("Không tìm thấy tin tuyển dụng"));

        if (!JobpostStatus.OPEN.equals(jobPost.getJobPostStatus())) {
            throw new GlobalException(400, "Tin tuyển dụng chưa mở");
        }
        boolean existed = expertApplicationRepo.existsByJobpostAndExpertProfile(jobPost, expertProfile);
        if (existed) {
            throw new GlobalException(400, "Bạn đã ứng tuyển tin tuyển dụng này");
        }
        ExpertApplication expertApplication = expertApplicationMapper.toEntity(request,jobPost,expertProfile);
        ExpertApplication savedExpertApplication = expertApplicationRepo.save(expertApplication);
        ExpertProposalResponse expertProposalResponse = null;

        if(request.getProposal() != null) {
            ExpertProposal expertProposal = expertProposalMapper.toEntity(request.getProposal(),savedExpertApplication);
            ExpertProposal saveexpertProposal = expertProposalRepo.save(expertProposal);
            savedExpertApplication.setExpertProposal(saveexpertProposal);

            expertProposalResponse = expertProposalMapper.toResponse(saveexpertProposal,saveexpertProposal.getDetailContent(),true );

        } notificationService.notifyExpertApplied(savedExpertApplication);
        return expertApplicationMapper.toResponse(savedExpertApplication,expertProposalResponse);
    }
    // Client owner của JobPost xem danh sách expert đã apply vào JobPost đó.
    // Nếu proposal chưa unlock thì detailContent trả null.
    @Override
    public List<ExpertApplicationResponse> getApplicationsByJobPost(Long jobPostId) {
        ClientProfile clientProfile = getCurrentClientProfile();
        JobPost jobPost = jobPostRepo.findJobPostByjobPostId(jobPostId).orElseThrow(() -> new GlobalException("Không tìm thấy tin tuyển dụng"));

        checkJobPostOwner(jobPost,clientProfile);

        return expertApplicationRepo.findByJobpost(jobPost)
                .stream()
                .map(application -> mapApplicationForClient(application, clientProfile))
                .toList();
    }
    // Expert xem danh sách application của chính mình.
    // Expert owner luôn được xem detailContent proposal của mình.
    @Override
    public List<ExpertApplicationResponse> getMyApplications() {
        ExpertProfile expertProfile = getCurrentExpertProfile();
        return expertApplicationRepo.findByExpertProfile(expertProfile).stream().map(this :: mapApplicationForExpert).toList();

    }
    // Xem chi tiết 1 application.
    // Nếu người xem là expert owner thì thấy full proposal.
    // Nếu người xem là client owner của JobPost thì chỉ thấy detailContent khi đã unlock.
    @Override
    public ExpertApplicationResponse getApplicationDetail(Long applicationId) {
        ExpertApplication expertApplication = getExpertApplication(applicationId);
        if(isCurrnetExpertOwner(expertApplication)) {
            return mapApplicationForExpert(expertApplication);
        }
        ClientProfile clientProfile = getCurrentClientProfile();
        checkJobPostOwner(expertApplication.getJobpost(),clientProfile);
        return mapApplicationForClient(expertApplication, clientProfile);
    }

    // Client mua mở khóa proposal detailContent.
    @Override
    public ExpertApplicationResponse unlockProposal(Long proposalId) {
        proposalPurchaseService.purchaseProposalByWallet(proposalId);

        ClientProfile clientProfile = getCurrentClientProfile();
        ExpertProposal expertProposal = getProposalById(proposalId);
        ExpertApplication expertApplication = getExpertApplication(
                expertProposal.getExpertApplication().getApplicationId()
        );

        return mapApplicationForClient(expertApplication, clientProfile);
    }




    // Map application cho client.
    // Client không phải lúc nào cũng được xem detailContent.
    private ExpertApplicationResponse mapApplicationForClient(ExpertApplication application, ClientProfile clientProfile
    ) {
        ExpertProposalResponse proposalResponse = mapProposalForClient(
                application.getExpertProposal(),
                clientProfile
        );
        return expertApplicationMapper.toResponse(application, proposalResponse);
    }

    // Map application cho expert owner.
    // Expert luôn xem được nội dung proposal mình đã gửi.
    private ExpertApplicationResponse mapApplicationForExpert(ExpertApplication expertApplication) {
        ExpertProposalResponse proposalResponse = mapProposalForExpert(
                expertApplication.getExpertProposal()
        );

        return expertApplicationMapper.toResponse(expertApplication, proposalResponse);
    }


// Map proposal cho client.
private ExpertProposalResponse mapProposalForClient(ExpertProposal expertProposal ,ClientProfile clientProfile){
    if(expertProposal == null ||Boolean.TRUE.equals(expertProposal.getIsDeleted())) {
        return null;
    }
    Boolean isUnlocked = proposalUnlockRepo.existsByProposalAndClientProfileAndIsUnlockedTrue(expertProposal,clientProfile);
    String detailContent = isUnlocked ? expertProposal.getDetailContent() : null;
    return expertProposalMapper.toResponse(expertProposal,detailContent,isUnlocked);
}


   // Map proposal có detail cho đúng expert sở hữu.
   private ExpertProposalResponse mapProposalForExpert(ExpertProposal expertProposal) {
     if(expertProposal == null ||Boolean.TRUE.equals(expertProposal.getIsDeleted())) {
           return null;
       }
       return expertProposalMapper.toResponse(expertProposal,expertProposal.getDetailContent(),true);
   }


    // Tìm application theo id, không có thì báo lỗi.
    private ExpertApplication getExpertApplication(Long applicationId) {
        return expertApplicationRepo.findByApplicationId(applicationId) .orElseThrow(() -> new GlobalException("Không tìm thấy hồ sơ ứng tuyển"));
    }
    // Tìm proposal theo id, không có thì báo lỗi.
    private ExpertProposal getProposalById(Long proposalId) {
        return expertProposalRepo.findExpertProposalByProposalId(proposalId).orElseThrow(() ->  new GlobalException("Không tìm thấy đề xuất"));
    }

    // Lấy ExpertProfile của user đang đăng nhập.
    private ExpertProfile getCurrentExpertProfile() {
        User user = currentUserService.getCurrentUser();
        return expertProfileRepo.findByUser(user).orElseThrow(() -> new GlobalException(403, "Only expert can use this API"));
    }

    // Lấy ClientProfile của user đang đăng nhập.
    private ClientProfile getCurrentClientProfile() {
        User user = currentUserService.getCurrentUser();
        return clientProfileRepo.findByUser(user).orElseThrow(() -> new GlobalException(403, "Chỉ khách hàng mới có thể dùng API này"));
    }

    // Kiểm tra client hiện tại có phải owner của JobPost không.
    // Client chỉ được xem applications/unlock proposal của job do mình tạo.
    private void checkJobPostOwner(JobPost jobPost , ClientProfile clientProfile) {
        if(!jobPost.getClientProfile().getClientProfileId().equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "Bạn không phải chủ sở hữu tin tuyển dụng này");
        }
    }

    // Kiểm tra user đang đăng nhập có phải expert sở hữu application không.
   public Boolean isCurrnetExpertOwner(ExpertApplication expertApplication) {
       User user = currentUserService.getCurrentUser();

       return expertProfileRepo.findByUser(user).map(
               expertProfile -> (expertApplication.getExpertProfile().getExpertProfileId().equals(expertProfile.getExpertProfileId()))
       ).orElse(false);
   }

    // Kiểm tra input của application: timeline, shortMessage và proposal không bắt buộc.
    private void validateApplicationInput(ExpertApplicationRequest request) {
        if (request == null) {
            throw new GlobalException(400, "Yêu cầu là bắt buộc");
        }

        contentManagerService.validateKeywordInput(request.getEstimatedTimeline());

        if (request.getShortMessage() != null) {
            contentManagerService.validateKeywordInput(request.getShortMessage());
        }

        if (request.getProposal() != null) {
            validateProposalInput(request.getProposal());
        }
    }

    // Validate input của proposal nếu expert gửi kèm proposal khi apply.
    private void validateProposalInput(ExpertProposalRequest request) {
        if (request == null) {
            throw new GlobalException(400, "Yêu cầu đề xuất là bắt buộc");
        }

        contentManagerService.validateKeywordInput(request.getTitle());

        if (request.getTechnologies() != null) {
            contentManagerService.validateKeywordInput(request.getTechnologies());
        }

        contentManagerService.validateKeywordInput(request.getDetailContent());
    }

    // Hàm kiểm tra chứng chỉ Expert đã được admin chấp nhận trước khi cho dùng nghiệp vụ kinh doanh.
    private void ensureExpertVerified(ExpertProfile expertProfile) {
        expertVerificationGuardService.ensureVerified(expertProfile);
    }

}
