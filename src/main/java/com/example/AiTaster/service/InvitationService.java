package com.example.AiTaster.service;

import com.example.AiTaster.constant.EscrowStatus;
import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.constant.TimelineUnit;
import com.example.AiTaster.dto.request.InvitationAcceptRequest;
import com.example.AiTaster.dto.request.InvitationCreateRequest;
import com.example.AiTaster.dto.response.InvitationDraftResponse;
import com.example.AiTaster.dto.response.InvitationResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertApplicationMapper;
import com.example.AiTaster.mapper.InvitationMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.imp.Iinvitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvitationService implements Iinvitation {
    private final ExpertApplicationMapper expertApplicationMapper;
    private final ExpertApplicationRepo expertApplicationRepo;
    private final ContentManagerService contentManagerService;
    private final ExpertProfileRepo expertProfileRepo;
    private final CurrentUserService currentUserService;
    private final ClientProfileRepo clientProfileRepo;
    private final JobPostRepo jobPostRepo;
    private final InvitationRepo invitationRepo;
    private final InvitationMapper invitationMapper;
    private final NotificationService notificationService;
    private final RealtimeService realtimeService;
    private final ProjectRepo projectRepo;


  // đẩy dữ liệu lên form cho client xem
    @Override
    public InvitationDraftResponse getDraftByApplication(Long applicationId) {
              ClientProfile clientProfile = getCurrentClientProfile();
              ExpertApplication expertApplication = getExpertApplication(applicationId);
              checkJobPostOwner(expertApplication.getJobpost(), clientProfile);
        return invitationMapper.toResponse(expertApplication);
    }

    @Override
    @Transactional
    public InvitationResponse createInvitation(InvitationCreateRequest request) {
        validateInvitationInput(request);
        ClientProfile clientProfile = getCurrentClientProfile();
        ExpertApplication expertApplication = getExpertApplication(request.getApplicationId());

        checkJobPostOwner(expertApplication.getJobpost(),clientProfile);
        pendingInvitationsExpire(); //đổi trạng thái lời mới quá hạn để thêm lời mới mới

        if(invitationRepo.existsByExpertApplication_JobpostAndInvitationStatus(expertApplication.getJobpost(),InvitationStatus.ACCEPTED))
        {
            throw new GlobalException(400,"Job post already has accepted invitation");
        }

        boolean hasPendingIntrueFour = invitationRepo.existsByExpertApplication_JobpostAndInvitationStatusAndExpiresAtAfter(expertApplication.getJobpost(),InvitationStatus.PENDING,LocalDateTime.now());

        if (hasPendingIntrueFour) {
            throw new GlobalException(400, "Job post already has pending invitation in 24 hours");
        }

        Invitation invitation = invitationMapper.toEntity(request,expertApplication);
        invitation.setInvitationStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setClientAcceptedTerms(request.getClientAcceptedTerms());
        invitation.setExpertAcceptedTerms(false);
        invitation.setRespondedAt(null);
        invitation.setFinalTimelineValue(request.getFinalTimelineValue());
        invitation.setFinalTimelineUnit(request.getFinalTimelineUnit());
        invitation.setFinalTimeline(buildFinalTimeline(request.getFinalTimelineValue(),request.getFinalTimelineUnit()));

        Invitation saveInvitation = invitationRepo.save(invitation);

        notificationService.notifyInvitationSent(saveInvitation);
        realtimeService.pushInvitationParticipants(
                saveInvitation,
                "INVITATION_CREATED",
                "Invitation created"
        );

        return invitationMapper.toResponseInvitation(saveInvitation);
    }
 // client xem đc danh sách của mình
    @Override
    public List<InvitationResponse> getMyClientInvitations() {
        ClientProfile clientProfile = getCurrentClientProfile();
        pendingInvitationsExpire();

        return invitationRepo.findByExpertApplication_Jobpost_ClientProfileOrderByCreateAtDesc(clientProfile)
                .stream()
                .filter(invitation -> !Boolean.TRUE.equals(invitation.getClientDeleted()))
                .map(invitationMapper::toResponseInvitation)
                .toList();

    }
//Expert xem danh sách invitation mình nhận được
    @Override
    public List<InvitationResponse> getMyExpertInvitations() {
        ExpertProfile expertProfile = getCurrentExpertProfile();
        pendingInvitationsExpire();
        return invitationRepo.findByExpertApplication_ExpertProfileOrderByCreateAtDesc(expertProfile)
                .stream()
                .filter(invitation -> !Boolean.TRUE.equals(invitation.getExpertDeleted()))
                .map(invitationMapper :: toResponseInvitation).toList();

    }
//expert hoặc client xem chung chi tiết invitations
    @Override
    public InvitationResponse getInvitationDetail(Long invitationId) {
        Invitation invitation = getInvitationWithDetail(invitationId);
        refreshInvitationTimeoutStatus(invitation); // Check timeout bằng giờ server trước khi trả về FE.
        expireIfNeeded(invitation);
        if(isCurrentInvitedExpert(invitation)) {
            return invitationMapper.toResponseInvitation(invitation);
        }
        ClientProfile clientProfile = getCurrentClientProfile();  // Nếu không phải expert thì kiểm tra client.
        checkInvitationOwnerClient(invitation,clientProfile);   // Chỉ client chủ job được xem.
        return invitationMapper.toResponseInvitation(invitation);
    }
    @Transactional
    @Override
    public InvitationResponse acceptInvitation(Long invitationId, InvitationAcceptRequest request) {
        if(request == null || !Boolean.TRUE.equals(request.getExpertAcceptedTerms())) {
            throw new GlobalException(400,"Expert accepted terms are not set");
        }
        Invitation invitation = getInvitationWithDetail(invitationId);

        ExpertProfile expertProfile = getCurrentExpertProfile();

        checkInvitedExpert(invitation,expertProfile);
        ensurePendingAndNotExpired(invitation);

        invitation.setExpertAcceptedTerms(true);

        invitation.setInvitationStatus(InvitationStatus.ACCEPTED);

        invitation.setRespondedAt(LocalDateTime.now());

        JobPost jobPost = invitation.getExpertApplication().getJobpost();
        jobPost.setJobPostStatus(JobpostStatus.CLOSED);

        Invitation saveInvitation = invitationRepo.saveAndFlush(invitation);
        jobPostRepo.updateJobPostStatus(jobPost.getJobPostId(), JobpostStatus.CLOSED);

        notificationService.notifyInvitationAccepted(saveInvitation);
        realtimeService.pushInvitationParticipants(
                saveInvitation,
                "INVITATION_ACCEPTED",
                "Invitation accepted"
        );

        return invitationMapper.toResponseInvitation(saveInvitation);
    }

    @Transactional
    @Override
    public void deleteInvitation(Long invitationId) {
        Invitation invitation = getInvitationWithDetail(invitationId);

        expireIfNeeded(invitation);

        if (InvitationStatus.PENDING.equals(invitation.getInvitationStatus())) {
            throw new GlobalException(400, "Pending invitation cannot be deleted");
        }

        markInvitationDeletedForCurrentUser(invitation);
        Invitation savedInvitation = invitationRepo.save(invitation);
        realtimeService.pushInvitationParticipants(
                savedInvitation,
                "INVITATION_DELETED",
                "Invitation deleted"
        );
    }

    @Override
    public InvitationResponse rejectInvitation(Long invitationId) {
        ExpertProfile expertProfile = getCurrentExpertProfile();
        Invitation invitation = getInvitationWithDetail(invitationId);
        checkInvitedExpert(invitation,expertProfile);
        ensurePendingAndNotExpired(invitation);
        invitation.setExpertAcceptedTerms(false);
        invitation.setInvitationStatus(InvitationStatus.REJECTED);
        invitation.setRespondedAt(LocalDateTime.now());
        Invitation saveInvitation = invitationRepo.save(invitation);

        notificationService.notifyInvitationRejected(saveInvitation);
        realtimeService.pushInvitationParticipants(
                saveInvitation,
                "INVITATION_REJECTED",
                "Invitation rejected"
        );

        return invitationMapper.toResponseInvitation(saveInvitation);
    }

    // Đổi các invitation PENDING quá 24h sang EXPIRED theo kiểu lazy expire.
    public void pendingInvitationsExpire() {
        List<Invitation> expiredInvitations  = invitationRepo.findByInvitationStatusAndExpiresAtBefore(InvitationStatus.PENDING, LocalDateTime.now());
        if(expiredInvitations.isEmpty()) {
            return;
        }
        expiredInvitations.forEach(invitation -> {
            invitation.setInvitationStatus(InvitationStatus.EXPIRED);
            invitation.setRespondedAt(null);
        });
        invitationRepo.saveAll(expiredInvitations);
    }

    // Nếu invitation đang PENDING nhưng quá hạn thì đổi sang EXPIRED.
    //kiểm tra thời gian hết hạn của lời mời có nằm trước thời điểm hiện tại hay không
    private void expireIfNeeded(Invitation invitation) {
        if (InvitationStatus.PENDING.equals(invitation.getInvitationStatus())
                && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setInvitationStatus(InvitationStatus.EXPIRED);
            invitationRepo.save(invitation);
        }
    }
    // Chỉ cho accept/reject nếu invitation còn PENDING và chưa hết hạn.
    private void ensurePendingAndNotExpired(Invitation invitation) {
        expireIfNeeded(invitation);

        if (!InvitationStatus.PENDING.equals(invitation.getInvitationStatus())) {
            throw new GlobalException(400, "Invitation is not pending");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setInvitationStatus(InvitationStatus.EXPIRED);
            invitationRepo.save(invitation);
            throw new GlobalException(400, "Invitation expired");
        }
    }
    // Validate timeline có thể dùng để tính toán.
    private void  validateTimeLine(Integer value, TimelineUnit unit) {
        if(value == null || unit == null) {
            throw new GlobalException(400,"Timeline is required");
        }
        if(value <= 0 ) {
            throw new GlobalException(400, "Timeline value must be greater than 0");
        }
    }
    // sinh text hiển thị timeLine từ value + unit
    // ví dụ : 2 + WEEK = "2 tuần"
    private String buildFinalTimeline(Integer value,TimelineUnit unit) {
        validateTimeLine(value,unit);
        return value + " " + unit.getDisplayName();
    }
    // Tìm Expertapplication theo id, không có thì báo lỗi.
    private ExpertApplication getExpertApplication(Long applicationId) {
        return expertApplicationRepo.findByApplicationId(applicationId) .orElseThrow(() -> new GlobalException(404, "Application not found"));
    }
    // Lấy ExpertProfile của user đang đăng nhập.
    private ExpertProfile getCurrentExpertProfile() {
        User user = currentUserService.getCurrentUser();
        return expertProfileRepo.findByUser(user).orElseThrow(() -> new GlobalException(403, "Only expert can use this API"));
    }

    // Lấy ClientProfile của user đang đăng nhập.
    private ClientProfile getCurrentClientProfile() {
        User user = currentUserService.getCurrentUser();
        return clientProfileRepo.findByUser(user).orElseThrow(() -> new GlobalException(403, "Only client can use this API"));
    }

    // Check client hiện tại có phải owner của JobPost không.
    // Client chỉ được xem applications/unlock proposal của job do mình tạo.
    private void checkJobPostOwner(JobPost jobPost , ClientProfile clientProfile) {
        if(!jobPost.getClientProfile().getClientProfileId().equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not owner of this jobpost");
        }
    }
    private void validateInvitationInput(InvitationCreateRequest request) {
        if (request == null) {
            throw new GlobalException(400, "Invitation is required");
        }
        contentManagerService.validateKeywordInput(request.getProjectTitle());
        contentManagerService.validateKeywordInput(request.getFinalRequirement());
        contentManagerService.validateKeywordInput(request.getExpectedOutput());
        contentManagerService.validateKeywordInput(request.getAcceptanceCriteria());

        validateTimeLine(request.getFinalTimelineValue(), request.getFinalTimelineUnit());

        if (!Boolean.TRUE.equals(request.getClientAcceptedTerms())) {
            throw new GlobalException(400, "Client must accept terms");
        }
    }
    private void checkInvitationOwnerClient(Invitation invitation, ClientProfile clientProfile) {
        Long ownerClientId = invitation.getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getClientProfileId();

        if (!ownerClientId.equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not owner client of this invitation");
        }
    }


    private boolean isCurrentInvitedExpert(Invitation invitation) {
        User user = currentUserService.getCurrentUser();
        return expertProfileRepo.findByUser(user).map(expertProfile -> invitation.getExpertApplication().getExpertProfile().getExpertProfileId().equals(expertProfile.getExpertProfileId())).orElse(false);
    }

    // kiểm tra có phải expert này là chủ lời mời không
    private void checkInvitedExpert(Invitation invitation,ExpertProfile expertProfile) {
        Long invitationExpertId = invitation.getExpertApplication().getExpertProfile().getExpertProfileId();

        if (!invitationExpertId.equals(expertProfile.getExpertProfileId())) {
            throw new GlobalException(403, "You are not invited expert of this invitation");
        }

    }

    private void markInvitationDeletedForCurrentUser(Invitation invitation) {
        User user = currentUserService.getCurrentUser();

        boolean isOwnerClient = clientProfileRepo.findByUser(user)
                .map(clientProfile -> invitation.getExpertApplication()
                        .getJobpost()
                        .getClientProfile()
                        .getClientProfileId()
                        .equals(clientProfile.getClientProfileId()))
                .orElse(false);

        boolean isInvitedExpert = expertProfileRepo.findByUser(user)
                .map(expertProfile -> invitation.getExpertApplication()
                        .getExpertProfile()
                        .getExpertProfileId()
                        .equals(expertProfile.getExpertProfileId()))
                .orElse(false);

        if (!isOwnerClient && !isInvitedExpert) {
            throw new GlobalException(403, "You are not allowed to delete this invitation");
        }

        if (isOwnerClient) {
            invitation.setClientDeleted(true);
        }

        if (isInvitedExpert) {
            invitation.setExpertDeleted(true);
        }
    }

    private Invitation getInvitationWithDetail(Long invitationId) {
        return invitationRepo.findWithDetailByInvitationId(invitationId)
                .orElseThrow(() -> new GlobalException(404, "Invitation not found"));
    }

    private void refreshInvitationTimeoutStatus(Invitation invitation) {
        LocalDateTime now = LocalDateTime.now();
        if (InvitationStatus.PENDING.equals(invitation.getInvitationStatus())
                && invitation.getExpiresAt().isBefore(now)) {
            invitation.setInvitationStatus(InvitationStatus.EXPIRED); // Expert không phản hồi đúng hạn.
            invitation.setRespondedAt(null); // Hết hạn PENDING thì không có thời điểm phản hồi.
            invitationRepo.save(invitation); // Lưu status mới vào DB.
            return;
        }
        if (InvitationStatus.ACCEPTED.equals(invitation.getInvitationStatus())
                && invitation.getRespondedAt() != null
                && invitation.getRespondedAt().plusHours(24).isBefore(now)
                && !projectRepo.existsByInvitation(invitation)) {
            invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED); // Client không thanh toán đúng hạn.
            invitationRepo.save(invitation); // Lưu status mới vào DB.
        }
    }

}
