package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.EscrowStatus;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.ProjectEscrow;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.service.InvoiceEmailService;
import com.example.AiTaster.service.InvoiceService;
import com.example.AiTaster.service.MoneyMovementService;
import com.example.AiTaster.service.NotificationService;
import com.example.AiTaster.service.RealtimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectEscrowPayoutServiceTest {

    @Mock
    private ProjectEscrowRepo projectEscrowRepo;

    @Mock
    private MoneyMovementService moneyMovementService;

    @Mock
    private ProjectRepo projectRepo;

    @Mock
    private RealtimeService realtimeService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private InvoiceEmailService invoiceEmailService;

    @InjectMocks
    private ProjectEscrowPayoutService payoutService;

    // Kiểm tra release escrow sẽ gửi email invoice sau khi invoice project completion được tạo.
    @Test
    void releaseToExpert_sendsInvoiceEmailAfterProjectInvoiceCreated() {
        User expert = User.builder()
                .userId(20L)
                .build();
        ExpertProfile expertProfile = ExpertProfile.builder()
                .user(expert)
                .build();
        ExpertApplication expertApplication = ExpertApplication.builder()
                .expertProfile(expertProfile)
                .build();
        Invitation invitation = Invitation.builder()
                .expertApplication(expertApplication)
                .build();
        Project project = Project.builder()
                .projectId(30L)
                .title("AI chatbot")
                .invitation(invitation)
                .build();
        ProjectEscrow escrow = ProjectEscrow.builder()
                .projectEscrowId(40L)
                .projectId(30L)
                .heldAmount(BigDecimal.valueOf(2_000_000))
                .escrowStatus(EscrowStatus.HELD)
                .build();
        Invoices invoice = new Invoices();
        invoice.setInvoiceId(88L);

        when(projectEscrowRepo.findByProjectIdForUpdate(30L)).thenReturn(Optional.of(escrow));
        when(moneyMovementService.calculateFee(BigDecimal.valueOf(2_000_000))).thenReturn(BigDecimal.valueOf(1_800_000));
        when(projectRepo.save(project)).thenReturn(project);
        when(projectEscrowRepo.save(escrow)).thenReturn(escrow);
        when(invoiceService.createForCompletedProject(30L)).thenReturn(invoice);

        payoutService.releaseToExpert(project);

        verify(moneyMovementService).moneyTransactionManagement(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
        verify(invoiceEmailService).enqueueAndSendForInvoice(88L);
    }
}
