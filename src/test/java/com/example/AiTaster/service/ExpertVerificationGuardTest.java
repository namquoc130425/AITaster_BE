package com.example.AiTaster.service;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.example.AiTaster.dto.request.ExpertApplicationRequest;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.request.InvitationAcceptRequest;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.*;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.payment.ProposalPurchaseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertVerificationGuardTest {

    @Mock
    private ExpertApplicationMapper expertApplicationMapper;
    @Mock
    private ExpertProposalMapper expertProposalMapper;
    @Mock
    private ExpertProposalRepo expertProposalRepo;
    @Mock
    private ExpertApplicationRepo expertApplicationRepo;
    @Mock
    private ContentManagerService contentManagerService;
    @Mock
    private ExpertProfileRepo expertProfileRepo;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ClientProfileRepo clientProfileRepo;
    @Mock
    private ProposalUnlockRepo proposalUnlockRepo;
    @Mock
    private JobPostRepo jobPostRepo;
    @Mock
    private ProposalPurchaseService proposalPurchaseService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ExpertApplicationService expertApplicationService;

    @Mock
    private ExpertServiceMapper expertServiceMapper;
    @Mock
    private ExpertServiceRepo expertServiceRepo;
    @Mock
    private SkillRepo skillRepo;
    @Mock
    private CategoryRepo categoryRepo;
    @Mock
    private ServiceFileRepo serviceFileRepo;
    @Mock
    private LocalFileStorageService localFileStorageService;
    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;

    @InjectMocks
    private ExpertProductService expertProductService;

    @Mock
    private InvitationMapper invitationMapper;
    @Mock
    private InvitationRepo invitationRepo;
    @Mock
    private RealtimeService realtimeService;
    @Mock
    private ProjectRepo projectRepo;

    @InjectMocks
    private InvitationService invitationService;

    @Test
    void applyJobPost_blocksExpertWhoseCertificateIsNotVerified() {
        User user = User.builder().userId(1L).build();
        ExpertProfile expertProfile = unverifiedExpert(user);
        ExpertApplicationRequest request = new ExpertApplicationRequest();
        request.setExpectedPrice(BigDecimal.valueOf(1_000_000));
        request.setEstimatedTimeline("7 days");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(expertProfileRepo.findByUser(user)).thenReturn(Optional.of(expertProfile));

        assertThatThrownBy(() -> expertApplicationService.applyJobPost(10L, request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Expert must be verified by admin before using this feature");

        verify(jobPostRepo, never()).findJobPostByjobPostId(10L);
    }

    @Test
    void createService_blocksExpertWhoseCertificateIsNotVerified() {
        User user = User.builder().userId(1L).build();
        ExpertProfile expertProfile = unverifiedExpert(user);
        ExpertServiceRequest request = new ExpertServiceRequest();
        request.setServiceName("AI chatbot");
        request.setServiceDescription("Build chatbot automation");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(expertProfileRepo.findByUser(user)).thenReturn(Optional.of(expertProfile));

        assertThatThrownBy(() -> expertProductService.CreatService(request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Expert must be verified by admin before using this feature");

        verify(categoryRepo, never()).getCategoriesByCategoryId(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void acceptInvitation_blocksExpertWhoseCertificateIsNotVerified() {
        User user = User.builder().userId(1L).build();
        ExpertProfile expertProfile = unverifiedExpert(user);
        Invitation invitation = Invitation.builder()
                .invitationId(10L)
                .expertApplication(ExpertApplication.builder()
                        .expertProfile(expertProfile)
                        .jobpost(JobPost.builder().build())
                        .build())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        InvitationAcceptRequest request = InvitationAcceptRequest.builder()
                .expertAcceptedTerms(true)
                .build();

        when(invitationRepo.findWithDetailByInvitationId(10L)).thenReturn(Optional.of(invitation));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(expertProfileRepo.findByUser(user)).thenReturn(Optional.of(expertProfile));

        assertThatThrownBy(() -> invitationService.acceptInvitation(10L, request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Expert must be verified by admin before using this feature");

        verify(jobPostRepo, never()).updateJobPostStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private ExpertProfile unverifiedExpert(User user) {
        ExpertProfile expertProfile = ExpertProfile.builder()
                .expertProfileId(20L)
                .user(user)
                .build();
        ExpertVerification verification = ExpertVerification.builder()
                .expertProfile(expertProfile)
                .verificationStatus(ExpertVerificationStatus.SUBMITTED)
                .certificateUrl("https://supabase.example/cert.pdf")
                .build();
        expertProfile.setVerification(verification);
        return expertProfile;
    }
}
