package com.example.AiTaster.service;

import com.example.AiTaster.constant.EscrowStatus;
import com.example.AiTaster.constant.MilestoneStatus;
import com.example.AiTaster.constant.MilestoneStep;
import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.ProjectEscrow;
import com.example.AiTaster.entity.ProjectMilestone;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.mapper.DeliverableMapper;
import com.example.AiTaster.mapper.ProjectMilestoneMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.DeliverableRepo;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectMilestoneRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.repository.ServiceFileRepo;
import com.example.AiTaster.service.payment.ProjectEscrowPayoutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMilestoneAutoReleaseTest {

    @Mock private ProjectMilestoneRepo projectMilestoneRepo;
    @Mock private DeliverableRepo deliverableRepo;
    @Mock private ServiceFileRepo serviceFileRepo;
    @Mock private ProjectRepo projectRepo;
    @Mock private ProjectEscrowRepo projectEscrowRepo;
    @Mock private ExpertProfileRepo expertProfileRepo;
    @Mock private ClientProfileRepo clientProfileRepo;
    @Mock private CurrentUserService currentUserService;
    @Mock private LocalFileStorageService localFileStorageService;
    @Mock private ProjectMilestoneMapper projectMilestoneMapper;
    @Mock private DeliverableMapper deliverableMapper;
    @Mock private SimpMessagingTemplate simpMessagingTemplate;
    @Mock private ProjectEscrowPayoutService projectEscrowPayoutService;
    @Mock private RealtimeService realtimeService;
    @Mock private NotificationService notificationService;
    @Mock private MilestoneTimePolicy milestoneTimePolicy;

    @InjectMocks
    private ProjectMilestoneService service;

    private Project buildProject(Long projectId, ProjectStatus status) {
        User clientUser = User.builder().userId(1L).build();
        User expertUser = User.builder().userId(2L).build();
        ClientProfile clientProfile = ClientProfile.builder().clientProfileId(10L).user(clientUser).build();
        ExpertProfile expertProfile = ExpertProfile.builder().expertProfileId(20L).user(expertUser).build();
        JobPost jobPost = JobPost.builder().jobPostId(30L).clientProfile(clientProfile).build();
        ExpertApplication expertApplication = ExpertApplication.builder()
                .jobpost(jobPost)
                .expertProfile(expertProfile)
                .build();
        Invitation invitation = Invitation.builder().expertApplication(expertApplication).build();
        return Project.builder()
                .projectId(projectId)
                .projectStatus(status)
                .invitation(invitation)
                .build();
    }

    @Test
    void autoReleaseOverdueFinalMilestone_releasesEscrowWhenEligible() {
        ProjectMilestone milestone = ProjectMilestone.builder()
                .milestoneId(1L)
                .projectId(100L)
                .currentStep(MilestoneStep.FINAL_CONFIRMATION)
                .status(MilestoneStatus.WAITING_CLIENT_REVIEW)
                .step1ApprovedAt(LocalDateTime.now().minusDays(10))
                .step2ApprovedAt(LocalDateTime.now().minusDays(4))
                .build();

        Project project = buildProject(100L, ProjectStatus.ACTIVE);

        ProjectEscrow escrow = ProjectEscrow.builder()
                .projectEscrowId(500L)
                .projectId(100L)
                .escrowStatus(EscrowStatus.HELD)
                .build();

        when(projectMilestoneRepo.findById(1L)).thenReturn(Optional.of(milestone));
        when(projectRepo.findWithDetailByProjectId(100L)).thenReturn(Optional.of(project));
        when(projectEscrowRepo.findByProjectId(100L)).thenReturn(Optional.of(escrow));

        service.autoReleaseOverdueFinalMilestone(1L);

        assertThat(milestone.getStatus()).isEqualTo(MilestoneStatus.COMPLETED);
        assertThat(milestone.getFinalApprovedAt()).isNotNull();
        verify(projectEscrowPayoutService).releaseToExpert(project);
        verify(notificationService).notify(any(), any(), any(), any(), any(), any());
    }

    @Test
    void autoReleaseOverdueFinalMilestone_skipsWhenProjectDisputed() {
        ProjectMilestone milestone = ProjectMilestone.builder()
                .milestoneId(2L)
                .projectId(200L)
                .currentStep(MilestoneStep.FINAL_CONFIRMATION)
                .status(MilestoneStatus.WAITING_CLIENT_REVIEW)
                .step1ApprovedAt(LocalDateTime.now().minusDays(10))
                .step2ApprovedAt(LocalDateTime.now().minusDays(4))
                .build();

        Project disputedProject = buildProject(200L, ProjectStatus.DISPUTED);

        when(projectMilestoneRepo.findById(2L)).thenReturn(Optional.of(milestone));
        when(projectRepo.findWithDetailByProjectId(200L)).thenReturn(Optional.of(disputedProject));

        service.autoReleaseOverdueFinalMilestone(2L);

        assertThat(milestone.getStatus()).isEqualTo(MilestoneStatus.WAITING_CLIENT_REVIEW);
        assertThat(milestone.getFinalApprovedAt()).isNull();
        verify(projectEscrowPayoutService, never()).releaseToExpert(any());
    }

    @Test
    void autoReleaseOverdueFinalMilestone_skipsWhenAlreadyReleased() {
        ProjectMilestone milestone = ProjectMilestone.builder()
                .milestoneId(3L)
                .projectId(300L)
                .currentStep(MilestoneStep.FINAL_CONFIRMATION)
                .status(MilestoneStatus.COMPLETED)
                .finalApprovedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(projectMilestoneRepo.findById(3L)).thenReturn(Optional.of(milestone));

        service.autoReleaseOverdueFinalMilestone(3L);

        verify(projectRepo, never()).findWithDetailByProjectId(any());
        verify(projectEscrowPayoutService, never()).releaseToExpert(any());
    }
}
