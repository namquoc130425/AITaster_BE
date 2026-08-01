package com.example.AiTaster.service;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.dto.response.PaymentTransactionResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserBankAccount;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.UserWalletMapper;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.UserWalletRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserWalletServiceTest {

    @Mock
    private UserWalletRepo userWalletRepo;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private UserWalletMapper userWalletMapper;
    @Mock
    private MoneyMovementService moneyMovementService;
    @Mock
    private UserBankAccountService userBankAccountService;
    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;
    @Mock
    private RealtimeService realtimeService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserWalletService userWalletService;

    @Test
    void getAdminTransactionsReturnsFilteredPageAndExcludesCanceledPayments() {
        User admin = User.builder().userId(1L).role(Role.ADMIN).build();
        PaymentTransaction transaction = PaymentTransaction.builder()
                .paymentTransactionId(10L)
                .senderId(2L)
                .receiverId(3L)
                .grossAmount(new BigDecimal("250000"))
                .feeAmount(new BigDecimal("25000"))
                .netAmount(new BigDecimal("225000"))
                .currency("VND")
                .transactionType(TransactionType.EXPERT_SERVICE_PURCHASE)
                .paymentMethod(PaymentMethod.WALLET)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(4L)
                .paymentReferenceType(PaymentReferenceType.EXPERT_SERVICE)
                .paymentCode("TEST-PAYMENT-CODE")
                .build();
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(currentUserService.getCurrentUser()).thenReturn(admin);
        when(paymentTransactionRepo.findAdminTransactions(
                eq(PaymentStatus.CANCELED),
                eq(PaymentStatus.SUCCESS),
                eq(TransactionType.EXPERT_SERVICE_PURCHASE),
                pageableCaptor.capture()
        )).thenReturn(new PageImpl<>(List.of(transaction)));

        PageResponse<PaymentTransactionResponse> result = userWalletService.getAdminTransactions(
                -1,
                500,
                PaymentStatus.SUCCESS,
                TransactionType.EXPERT_SERVICE_PURCHASE
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.getContent().get(0).getTransactionType())
                .isEqualTo(TransactionType.EXPERT_SERVICE_PURCHASE);
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);

        verify(paymentTransactionRepo).findAdminTransactions(
                PaymentStatus.CANCELED,
                PaymentStatus.SUCCESS,
                TransactionType.EXPERT_SERVICE_PURCHASE,
                pageableCaptor.getValue()
        );
    }

    @Test
    void getAdminTransactionsRejectsNonAdminUsers() {
        User client = User.builder().userId(2L).role(Role.CLIENT).build();
        when(currentUserService.getCurrentUser()).thenReturn(client);

        assertThatThrownBy(() -> userWalletService.getAdminTransactions(
                0,
                20,
                null,
                null
        )).isInstanceOf(GlobalException.class);

        verifyNoInteractions(paymentTransactionRepo);
    }

    @Test
    void requestWithdrawNotifiesAdminsAfterSavingRequest() {
        User expert = User.builder().userId(7L).fullName("Chuyên gia A").build();
        UserWallet wallet = UserWallet.builder()
                .userWalletId(11L)
                .user(expert)
                .balance(new BigDecimal("500000"))
                .frozenBalance(BigDecimal.ZERO)
                .currency("VND")
                .status(UserWalletStatus.ACTIVE)
                .requestWithdrawal(false)
                .amountRequestWithdrawal(BigDecimal.ZERO)
                .build();
        UserBankAccount bankAccount = UserBankAccount.builder().build();

        when(currentUserService.getCurrentUser()).thenReturn(expert);
        when(userWalletRepo.findById(11L)).thenReturn(java.util.Optional.of(wallet));
        when(userBankAccountService.getVerifiedBankAccountByUserId(7L)).thenReturn(bankAccount);
        when(userWalletRepo.save(wallet)).thenReturn(wallet);

        userWalletService.requestWithdraw(11L, new BigDecimal("200000"));

        verify(notificationService).notifyAdminWithdrawalRequested(wallet);
        verify(realtimeService).pushAdminWithdrawalEvent(
                "WITHDRAWAL_REQUESTED",
                11L,
                "Có yêu cầu rút tiền mới"
        );
    }
}
