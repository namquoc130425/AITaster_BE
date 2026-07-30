package com.example.AiTaster.service;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.repository.InvitationRepo;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.service.scheduler.InvitationPaymentExpirationScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationPaymentExpirationSchedulerTest {

    @Mock
    private InvitationRepo invitationRepo;

    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;

    @Mock
    private JobPostRepo jobPostRepo;

    @Mock
    private InvitationTimePolicy invitationTimePolicy;

    @InjectMocks
    private InvitationPaymentExpirationScheduler scheduler;

    @BeforeEach
    void setUpTimePolicy() {
        when(invitationTimePolicy.paymentCutoff(org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenAnswer(invocation -> invocation.<LocalDateTime>getArgument(0).minusHours(24));
    }

    @Test
    void expireInvitationAndPaymentDeadlines_expiresPendingInvitationAcceptedInvitationAndPendingSepayPayment() {
        Invitation pendingInvitation = Invitation.builder()
                .invitationId(1L)
                .invitationStatus(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .respondedAt(null)
                .build();

        JobPost jobPost = JobPost.builder()
                .jobPostId(99L)
                .jobPostStatus(JobpostStatus.CLOSED)
                .closedByInvitationId(2L)
                .build();
        ExpertApplication expertApplication = ExpertApplication.builder()
                .jobpost(jobPost)
                .build();
        Invitation acceptedInvitation = Invitation.builder()
                .invitationId(2L)
                .invitationStatus(InvitationStatus.ACCEPTED)
                .respondedAt(LocalDateTime.now().minusHours(25))
                .expertApplication(expertApplication)
                .build();
        PaymentTransaction pendingPayment = PaymentTransaction.builder()
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(invitationRepo.findByInvitationStatusAndExpiresAtBefore(eq(InvitationStatus.PENDING), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(pendingInvitation));
        when(invitationRepo.findAcceptedPaymentExpiredWithoutProject(eq(InvitationStatus.ACCEPTED), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(acceptedInvitation));
        when(paymentTransactionRepo.findByPaymentReferenceTypeAndReferenceIdInAndTransactionTypeAndPaymentStatusAndPaymentMethod(
                eq(PaymentReferenceType.INVITATION),
                eq(List.of(2L)),
                eq(TransactionType.PROJECT_ESCROW_DEPOSIT),
                eq(PaymentStatus.PENDING),
                eq(PaymentMethod.SEPAY)
        )).thenReturn(List.of(pendingPayment));

        scheduler.expireInvitationAndPaymentDeadlines();

        assertThat(pendingInvitation.getInvitationStatus()).isEqualTo(InvitationStatus.EXPIRED);
        assertThat(pendingInvitation.getRespondedAt()).isNull();
        assertThat(acceptedInvitation.getInvitationStatus()).isEqualTo(InvitationStatus.PAYMENT_EXPIRED);
        assertThat(pendingPayment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);

        verify(invitationRepo).saveAll(List.of(pendingInvitation));
        verify(invitationRepo).saveAll(List.of(acceptedInvitation));
        verify(paymentTransactionRepo).saveAll(List.of(pendingPayment));
        verify(jobPostRepo).reopenJobPost(99L, JobpostStatus.OPEN);
    }

    @Test
    void expireInvitationAndPaymentDeadlines_doesNotReopenJobPostClosedByAnotherInvitation() {
        JobPost jobPost = JobPost.builder()
                .jobPostId(100L)
                .jobPostStatus(JobpostStatus.CLOSED)
                .closedByInvitationId(777L) // đóng bởi invitation khác, không phải cái đang hết hạn
                .build();
        ExpertApplication expertApplication = ExpertApplication.builder()
                .jobpost(jobPost)
                .build();
        Invitation acceptedInvitation = Invitation.builder()
                .invitationId(3L)
                .invitationStatus(InvitationStatus.ACCEPTED)
                .respondedAt(LocalDateTime.now().minusHours(25))
                .expertApplication(expertApplication)
                .build();

        when(invitationRepo.findByInvitationStatusAndExpiresAtBefore(eq(InvitationStatus.PENDING), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(invitationRepo.findAcceptedPaymentExpiredWithoutProject(eq(InvitationStatus.ACCEPTED), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(acceptedInvitation));
        when(paymentTransactionRepo.findByPaymentReferenceTypeAndReferenceIdInAndTransactionTypeAndPaymentStatusAndPaymentMethod(
                eq(PaymentReferenceType.INVITATION),
                eq(List.of(3L)),
                eq(TransactionType.PROJECT_ESCROW_DEPOSIT),
                eq(PaymentStatus.PENDING),
                eq(PaymentMethod.SEPAY)
        )).thenReturn(List.of());

        scheduler.expireInvitationAndPaymentDeadlines();

        assertThat(acceptedInvitation.getInvitationStatus()).isEqualTo(InvitationStatus.PAYMENT_EXPIRED);
        verify(jobPostRepo, org.mockito.Mockito.never()).reopenJobPost(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void expireInvitationAndPaymentDeadlines_usesNowMinusTwentyFourHoursAsAcceptedPaymentDeadline() {
        ArgumentCaptor<LocalDateTime> deadlineCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        when(invitationRepo.findByInvitationStatusAndExpiresAtBefore(eq(InvitationStatus.PENDING), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(invitationRepo.findAcceptedPaymentExpiredWithoutProject(eq(InvitationStatus.ACCEPTED), deadlineCaptor.capture()))
                .thenReturn(List.of());

        LocalDateTime beforeRun = LocalDateTime.now().minusHours(24);
        scheduler.expireInvitationAndPaymentDeadlines();
        LocalDateTime afterRun = LocalDateTime.now().minusHours(24);

        assertThat(deadlineCaptor.getValue()).isBetween(beforeRun.minusSeconds(1), afterRun.plusSeconds(1));
        assertThat(Duration.between(deadlineCaptor.getValue(), LocalDateTime.now()).toHours()).isEqualTo(24);
    }
}
