package com.example.AiTaster.service;

import com.example.AiTaster.entity.User;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceWorkflowTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmailService emailService;

    @Test
    void queueExpertApplied_emailsClientWithBothNames() {
        User client = user("client@example.com", "Nguyễn Minh Anh");
        User expert = user("expert@example.com", "Trần Quốc Bảo");

        emailService.queueExpertApplied(client, expert, "Xây dựng chatbot bán hàng");

        WorkflowEmailRequestedEvent event = captureEvent();
        assertThat(event.recipientEmail()).isEqualTo("client@example.com");
        assertThat(event.subject()).isEqualTo("AITasker - Có chuyên gia mới ứng tuyển");
        assertThat(event.templateName()).isEqualTo("workflow-expert-applied");
        assertThat(event.variables())
                .containsEntry("clientName", "Nguyễn Minh Anh")
                .containsEntry("expertName", "Trần Quốc Bảo")
                .containsEntry("jobTitle", "Xây dựng chatbot bán hàng");
    }

    @Test
    void queueInvitationReceived_emailsExpertWithBothNames() {
        User client = user("client@example.com", "Nguyễn Minh Anh");
        User expert = user("expert@example.com", "Trần Quốc Bảo");

        emailService.queueInvitationReceived(client, expert, "Chatbot chăm sóc khách hàng");

        WorkflowEmailRequestedEvent event = captureEvent();
        assertThat(event.recipientEmail()).isEqualTo("expert@example.com");
        assertThat(event.subject()).isEqualTo("AITasker - Bạn nhận được lời mời dự án mới");
        assertThat(event.templateName()).isEqualTo("workflow-invitation-received");
        assertThat(event.variables())
                .containsEntry("clientName", "Nguyễn Minh Anh")
                .containsEntry("expertName", "Trần Quốc Bảo")
                .containsEntry("projectTitle", "Chatbot chăm sóc khách hàng");
    }

    @Test
    void queueInvitationAccepted_emailsClientWithBothNames() {
        User client = user("client@example.com", "Nguyễn Minh Anh");
        User expert = user("expert@example.com", "Trần Quốc Bảo");

        emailService.queueInvitationAccepted(client, expert, "Chatbot chăm sóc khách hàng");

        WorkflowEmailRequestedEvent event = captureEvent();
        assertThat(event.recipientEmail()).isEqualTo("client@example.com");
        assertThat(event.subject()).isEqualTo("AITasker - Chuyên gia đã chấp nhận lời mời");
        assertThat(event.templateName()).isEqualTo("workflow-invitation-accepted");
        assertThat(event.variables())
                .containsEntry("clientName", "Nguyễn Minh Anh")
                .containsEntry("expertName", "Trần Quốc Bảo")
                .containsEntry("projectTitle", "Chatbot chăm sóc khách hàng");
    }

    @Test
    void queueProjectStarted_emailsExpertWithBothNames() {
        User client = user("client@example.com", "Nguyễn Minh Anh");
        User expert = user("expert@example.com", "Trần Quốc Bảo");

        emailService.queueProjectStarted(client, expert, "Chatbot chăm sóc khách hàng");

        WorkflowEmailRequestedEvent event = captureEvent();
        assertThat(event.recipientEmail()).isEqualTo("expert@example.com");
        assertThat(event.subject()).isEqualTo("AITasker - Dự án đã chính thức bắt đầu");
        assertThat(event.templateName()).isEqualTo("workflow-project-started");
        assertThat(event.variables())
                .containsEntry("clientName", "Nguyễn Minh Anh")
                .containsEntry("expertName", "Trần Quốc Bảo")
                .containsEntry("projectTitle", "Chatbot chăm sóc khách hàng");
    }

    @Test
    void sendTemplateEmail_rendersTemplateAndVariablesThroughSharedMethod() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("workflow-expert-applied"), any(Context.class)))
                .thenReturn("<p>Email nghiệp vụ</p>");

        emailService.sendTemplateEmail(
                "client@example.com",
                "AITasker - Có chuyên gia mới ứng tuyển",
                "workflow-expert-applied",
                Map.of(
                        "clientName", "Nguyễn Minh Anh",
                        "expertName", "Trần Quốc Bảo",
                        "jobTitle", "Xây dựng chatbot bán hàng"
                )
        );

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("workflow-expert-applied"), contextCaptor.capture());
        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("client@example.com");
        assertThat(mimeMessage.getSubject()).isEqualTo("AITasker - Có chuyên gia mới ứng tuyển");
        assertThat(contextCaptor.getValue().getVariable("clientName")).isEqualTo("Nguyễn Minh Anh");
        assertThat(contextCaptor.getValue().getVariable("expertName")).isEqualTo("Trần Quốc Bảo");
    }

    private WorkflowEmailRequestedEvent captureEvent() {
        ArgumentCaptor<WorkflowEmailRequestedEvent> captor =
                ArgumentCaptor.forClass(WorkflowEmailRequestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }

    private User user(String email, String fullName) {
        return User.builder()
                .email(email)
                .fullName(fullName)
                .build();
    }
}
