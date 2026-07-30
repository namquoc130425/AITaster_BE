package com.example.AiTaster.service;

import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.mapper.NotificationMapper;
import com.example.AiTaster.repository.NotificationRepo;
import com.example.AiTaster.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceWorkflowEmailTest {

    @Mock
    private NotificationRepo notificationRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private User client;
    private User expert;
    private ExpertApplication application;
    private Invitation invitation;
    private Project project;

    @BeforeEach
    void setUp() {
        client = User.builder()
                .userId(10L)
                .email("client@example.com")
                .fullName("Nguyễn Minh Anh")
                .build();
        expert = User.builder()
                .userId(20L)
                .email("expert@example.com")
                .fullName("Trần Quốc Bảo")
                .build();

        ClientProfile clientProfile = ClientProfile.builder()
                .user(client)
                .build();
        ExpertProfile expertProfile = ExpertProfile.builder()
                .user(expert)
                .build();
        JobPost jobPost = JobPost.builder()
                .jobPostId(30L)
                .title("Xây dựng chatbot bán hàng")
                .clientProfile(clientProfile)
                .build();
        application = ExpertApplication.builder()
                .applicationId(40L)
                .jobpost(jobPost)
                .expertProfile(expertProfile)
                .build();
        invitation = Invitation.builder()
                .invitationId(50L)
                .projectTitle("Chatbot chăm sóc khách hàng")
                .expertApplication(application)
                .build();
        project = Project.builder()
                .projectId(60L)
                .title("Chatbot chăm sóc khách hàng")
                .invitation(invitation)
                .build();
    }

    @Test
    void notifyExpertApplied_queuesEmailForClient() {
        notificationService.notifyExpertApplied(application);

        verify(emailService).queueExpertApplied(
                client,
                expert,
                "Xây dựng chatbot bán hàng"
        );
    }

    @Test
    void notifyInvitationSent_queuesEmailForExpert() {
        notificationService.notifyInvitationSent(invitation);

        verify(emailService).queueInvitationReceived(
                client,
                expert,
                "Chatbot chăm sóc khách hàng"
        );
    }

    @Test
    void notifyInvitationAccepted_queuesEmailForClient() {
        notificationService.notifyInvitationAccepted(invitation);

        verify(emailService).queueInvitationAccepted(
                client,
                expert,
                "Chatbot chăm sóc khách hàng"
        );
    }

    @Test
    void notifyProjectWorkspaceReady_queuesProjectStartedEmailForExpert() {
        notificationService.notifyProjectWorkspaceReady(project);

        verify(emailService).queueProjectStarted(
                client,
                expert,
                "Chatbot chăm sóc khách hàng"
        );
    }
}
