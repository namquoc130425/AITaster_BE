package com.example.AiTaster.service;

import com.example.AiTaster.constant.InvoiceEmailRecipientRole;
import com.example.AiTaster.constant.InvoiceEmailStatus;
import com.example.AiTaster.constant.InvoiceEmailType;
import com.example.AiTaster.constant.InvoiceStatus;
import com.example.AiTaster.constant.InvoiceType;
import com.example.AiTaster.entity.InvoiceEmailLog;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.InvoiceEmailLogRepo;
import com.example.AiTaster.repository.InvoicesRepo;
import com.example.AiTaster.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceEmailServiceTest {

    @Mock
    private InvoicesRepo invoicesRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private InvoiceEmailLogRepo invoiceEmailLogRepo;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InvoiceEmailService invoiceEmailService;

    @Test
    void enqueueForInvoice_createsOneClientEmailLogForPaidAiServiceInvoice() {
        Invoices invoice = paidInvoice(10L, InvoiceType.SERVICE_ORDER, 100L, 200L);
        User client = User.builder()
                .userId(100L)
                .email("client@example.com")
                .build();

        when(invoicesRepo.findById(10L)).thenReturn(Optional.of(invoice));
        when(userRepo.findById(100L)).thenReturn(Optional.of(client));
        when(invoiceEmailLogRepo.findByInvoiceIdAndRecipientUserIdAndRecipientRoleAndEmailType(
                10L,
                100L,
                InvoiceEmailRecipientRole.CLIENT,
                InvoiceEmailType.SERVICE_ORDER_PAID
        )).thenReturn(Optional.empty());
        when(invoiceEmailLogRepo.save(any(InvoiceEmailLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invoiceEmailService.enqueueForInvoice(10L);

        ArgumentCaptor<InvoiceEmailLog> captor = ArgumentCaptor.forClass(InvoiceEmailLog.class);
        verify(invoiceEmailLogRepo).save(captor.capture());

        InvoiceEmailLog savedLog = captor.getValue();
        assertThat(savedLog.getInvoiceId()).isEqualTo(10L);
        assertThat(savedLog.getRecipientUserId()).isEqualTo(100L);
        assertThat(savedLog.getRecipientEmail()).isEqualTo("client@example.com");
        assertThat(savedLog.getRecipientRole()).isEqualTo(InvoiceEmailRecipientRole.CLIENT);
        assertThat(savedLog.getEmailType()).isEqualTo(InvoiceEmailType.SERVICE_ORDER_PAID);
        assertThat(savedLog.getStatus()).isEqualTo(InvoiceEmailStatus.PENDING);
        assertThat(savedLog.getSendAttemptCount()).isZero();
        assertThat(savedLog.getCreatedAt()).isNotNull();
        assertThat(savedLog.getUpdatedAt()).isNotNull();
    }

    @Test
    void enqueueForInvoice_createsClientAndExpertEmailLogsForCompletedProjectInvoice() {
        Invoices invoice = paidInvoice(20L, InvoiceType.PROJECT_COMPLETION, 101L, 202L);
        User client = User.builder()
                .userId(101L)
                .email("client@example.com")
                .build();
        User expert = User.builder()
                .userId(202L)
                .email("expert@example.com")
                .build();

        when(invoicesRepo.findById(20L)).thenReturn(Optional.of(invoice));
        when(userRepo.findById(101L)).thenReturn(Optional.of(client));
        when(userRepo.findById(202L)).thenReturn(Optional.of(expert));
        when(invoiceEmailLogRepo.findByInvoiceIdAndRecipientUserIdAndRecipientRoleAndEmailType(
                20L,
                101L,
                InvoiceEmailRecipientRole.CLIENT,
                InvoiceEmailType.PROJECT_COMPLETION_PAID
        )).thenReturn(Optional.empty());
        when(invoiceEmailLogRepo.findByInvoiceIdAndRecipientUserIdAndRecipientRoleAndEmailType(
                20L,
                202L,
                InvoiceEmailRecipientRole.EXPERT,
                InvoiceEmailType.PROJECT_COMPLETION_PAID
        )).thenReturn(Optional.empty());
        when(invoiceEmailLogRepo.save(any(InvoiceEmailLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invoiceEmailService.enqueueForInvoice(20L);

        ArgumentCaptor<InvoiceEmailLog> captor = ArgumentCaptor.forClass(InvoiceEmailLog.class);
        verify(invoiceEmailLogRepo, org.mockito.Mockito.times(2)).save(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(InvoiceEmailLog::getRecipientRole)
                .containsExactlyInAnyOrder(InvoiceEmailRecipientRole.CLIENT, InvoiceEmailRecipientRole.EXPERT);
        assertThat(captor.getAllValues())
                .extracting(InvoiceEmailLog::getRecipientEmail)
                .containsExactlyInAnyOrder("client@example.com", "expert@example.com");
    }

    @Test
    void enqueueForInvoice_doesNotCreateDuplicateEmailLogWhenOneAlreadyExists() {
        Invoices invoice = paidInvoice(10L, InvoiceType.SERVICE_ORDER, 100L, 200L);
        InvoiceEmailLog existingLog = InvoiceEmailLog.builder()
                .invoiceId(10L)
                .recipientUserId(100L)
                .recipientRole(InvoiceEmailRecipientRole.CLIENT)
                .emailType(InvoiceEmailType.SERVICE_ORDER_PAID)
                .status(InvoiceEmailStatus.PENDING)
                .sendAttemptCount(0)
                .build();

        when(invoicesRepo.findById(10L)).thenReturn(Optional.of(invoice));
        when(userRepo.findById(100L)).thenReturn(Optional.of(User.builder().userId(100L).email("client@example.com").build()));
        when(invoiceEmailLogRepo.findByInvoiceIdAndRecipientUserIdAndRecipientRoleAndEmailType(
                10L,
                100L,
                InvoiceEmailRecipientRole.CLIENT,
                InvoiceEmailType.SERVICE_ORDER_PAID
        )).thenReturn(Optional.of(existingLog));

        invoiceEmailService.enqueueForInvoice(10L);

        verify(invoiceEmailLogRepo, never()).save(any(InvoiceEmailLog.class));
    }

    @Test
    void enqueueForInvoice_rejectsInvoiceThatIsNotPaid() {
        Invoices invoice = paidInvoice(30L, InvoiceType.SERVICE_ORDER, 100L, 200L);
        invoice.setInvoiceStatus(InvoiceStatus.CANCELLED);

        when(invoicesRepo.findById(30L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceEmailService.enqueueForInvoice(30L))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Only paid invoices can be emailed");

        verify(invoiceEmailLogRepo, never()).save(any(InvoiceEmailLog.class));
    }

    @Test
    void sendEmailLog_marksSentAndIncrementsAttemptWhenMailSucceeds() {
        Invoices invoice = paidInvoice(40L, InvoiceType.SERVICE_ORDER, 100L, 200L);
        invoice.setDescription("AI Resume Builder");
        User client = User.builder()
                .userId(100L)
                .email("client@example.com")
                .fullName("Client Name")
                .build();
        InvoiceEmailLog emailLog = emailLog(50L, 40L, 100L, "client@example.com", 0);

        when(invoiceEmailLogRepo.findById(50L)).thenReturn(Optional.of(emailLog));
        when(invoicesRepo.findById(40L)).thenReturn(Optional.of(invoice));
        when(userRepo.findById(100L)).thenReturn(Optional.of(client));
        when(invoiceEmailLogRepo.save(any(InvoiceEmailLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceEmailLog result = invoiceEmailService.sendEmailLog(50L);

        assertThat(result.getStatus()).isEqualTo(InvoiceEmailStatus.SENT);
        assertThat(result.getSendAttemptCount()).isEqualTo(1);
        assertThat(result.getSentAt()).isNotNull();
        assertThat(result.getLastError()).isNull();
        verify(emailService).sendInvoiceEmail(
                eq("client@example.com"),
                eq(InvoiceEmailRecipientRole.CLIENT),
                eq(InvoiceEmailType.SERVICE_ORDER_PAID),
                eq(invoice),
                eq("Client Name"),
                eq("AI Resume Builder"),
                eq("AI Resume Builder")
        );
    }

    @Test
    void sendEmailLog_marksFailedAndIncrementsAttemptWhenMailFails() {
        Invoices invoice = paidInvoice(41L, InvoiceType.SERVICE_ORDER, 100L, 200L);
        User client = User.builder()
                .userId(100L)
                .email("client@example.com")
                .fullName("Client Name")
                .build();
        InvoiceEmailLog emailLog = emailLog(51L, 41L, 100L, "client@example.com", 2);

        when(invoiceEmailLogRepo.findById(51L)).thenReturn(Optional.of(emailLog));
        when(invoicesRepo.findById(41L)).thenReturn(Optional.of(invoice));
        when(userRepo.findById(100L)).thenReturn(Optional.of(client));
        when(invoiceEmailLogRepo.save(any(InvoiceEmailLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP timeout")).when(emailService).sendInvoiceEmail(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        InvoiceEmailLog result = invoiceEmailService.sendEmailLog(51L);

        assertThat(result.getStatus()).isEqualTo(InvoiceEmailStatus.FAILED);
        assertThat(result.getSendAttemptCount()).isEqualTo(3);
        assertThat(result.getLastError()).isEqualTo("SMTP timeout");
        verify(invoiceEmailLogRepo).save(emailLog);
    }

    @Test
    void sendEmailLog_doesNotSendWhenMaxAttemptReached() {
        InvoiceEmailLog emailLog = emailLog(52L, 42L, 100L, "client@example.com", InvoiceEmailService.MAX_SEND_ATTEMPT);
        emailLog.setStatus(InvoiceEmailStatus.FAILED);

        when(invoiceEmailLogRepo.findById(52L)).thenReturn(Optional.of(emailLog));

        InvoiceEmailLog result = invoiceEmailService.sendEmailLog(52L);

        assertThat(result).isSameAs(emailLog);
        verify(emailService, never()).sendInvoiceEmail(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
        verify(invoiceEmailLogRepo, never()).save(any(InvoiceEmailLog.class));
    }

    private Invoices paidInvoice(Long invoiceId, InvoiceType invoiceType, Long clientId, Long expertId) {
        Invoices invoice = new Invoices();
        invoice.setInvoiceId(invoiceId);
        invoice.setInvoiceCode("INV-TEST");
        invoice.setClientId(clientId);
        invoice.setExpertId(expertId);
        invoice.setInvoiceType(invoiceType);
        invoice.setInvoiceStatus(InvoiceStatus.PAID);
        return invoice;
    }

    private InvoiceEmailLog emailLog(
            Long invoiceEmailLogId,
            Long invoiceId,
            Long recipientUserId,
            String recipientEmail,
            int sendAttemptCount
    ) {
        return InvoiceEmailLog.builder()
                .invoiceEmailLogId(invoiceEmailLogId)
                .invoiceId(invoiceId)
                .recipientUserId(recipientUserId)
                .recipientEmail(recipientEmail)
                .recipientRole(InvoiceEmailRecipientRole.CLIENT)
                .emailType(InvoiceEmailType.SERVICE_ORDER_PAID)
                .status(InvoiceEmailStatus.PENDING)
                .sendAttemptCount(sendAttemptCount)
                .build();
    }
}
