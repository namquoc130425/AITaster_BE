package com.example.AiTaster.service;

import com.example.AiTaster.constant.EscrowStatus;
import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.ProjectEscrow;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPaymentServiceTest {

    private static final Long PROJECT_ID = 10L;
    private static final Long OWNER_CLIENT_ID = 20L;
    private static final Long CURRENT_USER_ID = 30L;
    private static final Long ESCROW_ID = 40L;
    private static final BigDecimal PROJECT_AMOUNT = new BigDecimal("150000.00");

    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;

    @Mock
    private PaymentTransactionMapper paymentTransactionMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ClientProfileRepo clientProfileRepo;

    @Mock
    private ProjectRepo projectRepo;

    @Mock
    private ProjectEscrowRepo projectEscrowRepo;

    @InjectMocks
    private ProjectPaymentService projectPaymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(projectPaymentService, "sepayBankCode", "MB");
        ReflectionTestUtils.setField(projectPaymentService, "sepayAccountNumber", "123456789");
    }

    @Test
    void createProjectPayment_shouldThrowNotFound_whenProjectDoesNotExist() {
        when(projectRepo.findByProjectId(PROJECT_ID)).thenReturn(Optional.empty());

        assertGlobalException(
                () -> projectPaymentService.createProjectPayment(PROJECT_ID),
                404,
                "Project not found"
        );

        verifyNoInteractions(currentUserService, clientProfileRepo, projectEscrowRepo, paymentTransactionRepo, paymentTransactionMapper);
    }

    @Test
    void createProjectPayment_shouldThrowForbidden_whenCurrentUserIsNotClient() {
        Project project = waitingProjectWithDeadline(LocalDateTime.now().plusHours(1));
        User currentUser = currentUser();

        when(projectRepo.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(clientProfileRepo.findByUser(currentUser)).thenReturn(Optional.empty());

        assertGlobalException(
                () -> projectPaymentService.createProjectPayment(PROJECT_ID),
                403,
                "Only client can create project payment"
        );

        verifyNoInteractions(projectEscrowRepo, paymentTransactionRepo, paymentTransactionMapper);
    }

    @Test
    void createProjectPayment_shouldThrowForbidden_whenClientDoesNotOwnProject() {
        Project project = waitingProjectWithDeadline(LocalDateTime.now().plusHours(1));
        User currentUser = currentUser();
        ClientProfile otherClient = clientProfile(999L, currentUser);

        when(projectRepo.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(clientProfileRepo.findByUser(currentUser)).thenReturn(Optional.of(otherClient));

        assertGlobalException(
                () -> projectPaymentService.createProjectPayment(PROJECT_ID),
                403,
                "You are not owner client of this project"
        );

        verifyNoInteractions(projectEscrowRepo, paymentTransactionRepo, paymentTransactionMapper);
    }

    @Test
    void createProjectPayment_shouldThrowForbidden_whenProjectStatusIsNotWaitingEscrow() {
        Project project = project(ProjectStatus.ACTIVE, PROJECT_AMOUNT, LocalDateTime.now().plusHours(1), OWNER_CLIENT_ID);
        givenOwnerClientCanRequest(project);

        assertGlobalException(
                () -> projectPaymentService.createProjectPayment(PROJECT_ID),
                403,
                "Project status is not waiting escrow"
        );

        verifyNoInteractions(projectEscrowRepo, paymentTransactionRepo, paymentTransactionMapper);
    }

    @Test
    void createProjectPayment_shouldExpirePendingPaymentCancelEscrowAndProject_whenDeadlineExpired() {
        Project project = waitingProjectWithDeadline(LocalDateTime.now().minusMinutes(1));
        ProjectEscrow escrow = escrow();
        PaymentTransaction pendingPayment = pendingPayment(PROJECT_AMOUNT, "AIT-PROJ-10-OLD");
        givenOwnerClientCanRequest(project);

        when(paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                PaymentReferenceType.PROJECT,
                PROJECT_ID,
                PaymentStatus.PENDING,
                PaymentMethod.SEPAY
        )).thenReturn(Optional.of(pendingPayment));
        when(projectEscrowRepo.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(escrow));

        assertGlobalException(
                () -> projectPaymentService.createProjectPayment(PROJECT_ID),
                403,
                "Payment deadline is expired"
        );

        assertThat(pendingPayment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(escrow.getEscrowStatus()).isEqualTo(EscrowStatus.CANCELED);
        assertThat(project.getProjectStatus()).isEqualTo(ProjectStatus.CANCELED);
        verify(paymentTransactionRepo).save(same(pendingPayment));
        verify(projectEscrowRepo).save(same(escrow));
        verify(projectRepo).save(same(project));
        verifyNoInteractions(paymentTransactionMapper);
    }

    @Test
    void createProjectPayment_shouldThrowNotFound_whenProjectEscrowDoesNotExist() {
        Project project = waitingProjectWithDeadline(LocalDateTime.now().plusHours(1));
        givenOwnerClientCanRequest(project);

        when(projectEscrowRepo.findByProjectId(PROJECT_ID)).thenReturn(Optional.empty());

        assertGlobalException(
                () -> projectPaymentService.createProjectPayment(PROJECT_ID),
                404,
                "Project escrow not found"
        );

        verify(paymentTransactionRepo, never()).save(any());
        verifyNoInteractions(paymentTransactionMapper);
    }

    @Test
    void createProjectPayment_shouldReuseExistingPendingSepayPayment_whenPendingPaymentExists() {
        Project project = waitingProjectWithDeadline(LocalDateTime.now().plusHours(1));
        PaymentTransaction existingPayment = pendingPayment(PROJECT_AMOUNT, "AIT-PROJ-10-ABC 123");
        givenOwnerClientCanRequest(project);
        stubMapperResponse();

        when(projectEscrowRepo.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(escrow()));
        when(paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                PaymentReferenceType.PROJECT,
                PROJECT_ID,
                PaymentStatus.PENDING,
                PaymentMethod.SEPAY
        )).thenReturn(Optional.of(existingPayment));

        ProjectPaymentResponse response = projectPaymentService.createProjectPayment(PROJECT_ID);

        ArgumentCaptor<String> qrUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(paymentTransactionMapper).toProjectPaymentResponse(same(existingPayment), eq(PROJECT_ID), qrUrlCaptor.capture());
        verify(paymentTransactionRepo, never()).save(any());
        assertThat(response.getPaymentCode()).isEqualTo("AIT-PROJ-10-ABC 123");
        assertThat(qrUrlCaptor.getValue())
                .isEqualTo("https://qr.sepay.vn/img?bank=MB&acc=123456789&amount=150000&des=AIT-PROJ-10-ABC+123");
    }

    @Test
    void createProjectPayment_shouldCreatePendingSepayPayment_whenPendingPaymentDoesNotExist() {
        Project project = waitingProjectWithDeadline(LocalDateTime.now().plusHours(1));
        User currentUser = givenOwnerClientCanRequest(project);
        ProjectEscrow escrow = escrow();
        givenPaymentSaveReturnsSavedEntity();
        stubMapperResponse();

        when(projectEscrowRepo.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(escrow));
        when(paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                PaymentReferenceType.PROJECT,
                PROJECT_ID,
                PaymentStatus.PENDING,
                PaymentMethod.SEPAY
        )).thenReturn(Optional.empty());

        ProjectPaymentResponse response = projectPaymentService.createProjectPayment(PROJECT_ID);

        ArgumentCaptor<PaymentTransaction> paymentCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        ArgumentCaptor<String> qrUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(paymentTransactionRepo).save(paymentCaptor.capture());
        verify(paymentTransactionMapper).toProjectPaymentResponse(same(paymentCaptor.getValue()), eq(PROJECT_ID), qrUrlCaptor.capture());

        PaymentTransaction savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getProjectEscrowId()).isEqualTo(ESCROW_ID);
        assertThat(savedPayment.getSenderId()).isEqualTo(currentUser.getUserId());
        assertThat(savedPayment.getAmount()).isEqualByComparingTo(PROJECT_AMOUNT);
        assertThat(savedPayment.getCurrency()).isEqualTo("VND");
        assertThat(savedPayment.getTransactionType()).isEqualTo(TransactionType.PROJECT_ESCROW);
        assertThat(savedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.SEPAY);
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedPayment.getReferenceId()).isEqualTo(PROJECT_ID);
        assertThat(savedPayment.getPaymentReferenceType()).isEqualTo(PaymentReferenceType.PROJECT);
        assertThat(savedPayment.getProviderName()).isEqualTo("SEPAY");
        assertThat(savedPayment.getPaymentCode()).startsWith("AIT-PROJ-10-");
        assertThat(savedPayment.getPaymentCode()).hasSize("AIT-PROJ-10-".length() + 8);
        assertThat(savedPayment.getExpiredAt()).isEqualTo(project.getPaymentDeadlineAt());
        assertThat(response.getQrUrl()).isEqualTo(qrUrlCaptor.getValue());
        assertThat(qrUrlCaptor.getValue())
                .contains("bank=MB")
                .contains("acc=123456789")
                .contains("amount=150000")
                .contains("des=" + savedPayment.getPaymentCode());
    }

    @Test
    void createProjectPayment_shouldThrowArithmeticException_whenAmountHasFractionalVnd() {
        Project project = project(ProjectStatus.WAITING_ESCROW, new BigDecimal("150000.50"), LocalDateTime.now().plusHours(1), OWNER_CLIENT_ID);
        givenOwnerClientCanRequest(project);
        givenPaymentSaveReturnsSavedEntity();

        when(projectEscrowRepo.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(escrow()));
        when(paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                PaymentReferenceType.PROJECT,
                PROJECT_ID,
                PaymentStatus.PENDING,
                PaymentMethod.SEPAY
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectPaymentService.createProjectPayment(PROJECT_ID))
                .isInstanceOf(ArithmeticException.class);

        verify(paymentTransactionMapper, never()).toProjectPaymentResponse(any(), eq(PROJECT_ID), anyString());
    }

    private void assertGlobalException(Runnable action, int expectedCode, String expectedMessage) {
        assertThatThrownBy(action::run)
                .isInstanceOf(GlobalException.class)
                .hasMessage(expectedMessage)
                .satisfies(error -> assertThat(((GlobalException) error).getCode()).isEqualTo(expectedCode));
    }

    private User givenOwnerClientCanRequest(Project project) {
        User currentUser = currentUser();
        ClientProfile clientProfile = clientProfile(OWNER_CLIENT_ID, currentUser);

        when(projectRepo.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(clientProfileRepo.findByUser(currentUser)).thenReturn(Optional.of(clientProfile));

        return currentUser;
    }

    private void givenPaymentSaveReturnsSavedEntity() {
        when(paymentTransactionRepo.save(any(PaymentTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubMapperResponse() {
        when(paymentTransactionMapper.toProjectPaymentResponse(any(PaymentTransaction.class), eq(PROJECT_ID), anyString()))
                .thenAnswer(invocation -> {
                    PaymentTransaction payment = invocation.getArgument(0);
                    String qrUrl = invocation.getArgument(2);
                    return ProjectPaymentResponse.builder()
                            .paymentTransactionId(payment.getPaymentTransactionId())
                            .projectId(PROJECT_ID)
                            .senderId(payment.getSenderId())
                            .amount(payment.getAmount())
                            .currency(payment.getCurrency())
                            .transactionType(payment.getTransactionType())
                            .paymentMethod(payment.getPaymentMethod())
                            .paymentStatus(payment.getPaymentStatus())
                            .paymentReferenceType(payment.getPaymentReferenceType())
                            .referenceId(payment.getReferenceId())
                            .providerName(payment.getProviderName())
                            .paymentCode(payment.getPaymentCode())
                            .qrUrl(qrUrl)
                            .expiredAt(payment.getExpiredAt())
                            .build();
                });
    }

    private Project waitingProjectWithDeadline(LocalDateTime paymentDeadlineAt) {
        return project(ProjectStatus.WAITING_ESCROW, PROJECT_AMOUNT, paymentDeadlineAt, OWNER_CLIENT_ID);
    }

    private Project project(ProjectStatus status, BigDecimal amount, LocalDateTime paymentDeadlineAt, Long ownerClientId) {
        ClientProfile ownerProfile = ClientProfile.builder()
                .clientProfileId(ownerClientId)
                .build();
        JobPost jobPost = JobPost.builder()
                .clientProfile(ownerProfile)
                .build();
        ExpertApplication expertApplication = ExpertApplication.builder()
                .jobpost(jobPost)
                .build();
        Invitation invitation = Invitation.builder()
                .expertApplication(expertApplication)
                .build();

        return Project.builder()
                .projectId(PROJECT_ID)
                .projectStatus(status)
                .agreedPrice(amount)
                .paymentDeadlineAt(paymentDeadlineAt)
                .invitation(invitation)
                .build();
    }

    private ProjectEscrow escrow() {
        return ProjectEscrow.builder()
                .projectEscrowId(ESCROW_ID)
                .projectId(PROJECT_ID)
                .clientProfileId(OWNER_CLIENT_ID)
                .expertProfileId(88L)
                .agreedAmount(PROJECT_AMOUNT)
                .heldAmount(BigDecimal.ZERO)
                .platformFee(BigDecimal.ZERO)
                .expertAmount(PROJECT_AMOUNT)
                .escrowStatus(EscrowStatus.WAITING_PAYMENT)
                .build();
    }

    private PaymentTransaction pendingPayment(BigDecimal amount, String paymentCode) {
        return PaymentTransaction.builder()
                .paymentTransactionId(77L)
                .projectEscrowId(ESCROW_ID)
                .senderId(CURRENT_USER_ID)
                .amount(amount)
                .currency("VND")
                .transactionType(TransactionType.PROJECT_ESCROW)
                .paymentMethod(PaymentMethod.SEPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .referenceId(PROJECT_ID)
                .paymentReferenceType(PaymentReferenceType.PROJECT)
                .providerName("SEPAY")
                .paymentCode(paymentCode)
                .expiredAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    private User currentUser() {
        return User.builder()
                .userId(CURRENT_USER_ID)
                .username("client")
                .build();
    }

    private ClientProfile clientProfile(Long clientProfileId, User user) {
        return ClientProfile.builder()
                .clientProfileId(clientProfileId)
                .user(user)
                .build();
    }
}
