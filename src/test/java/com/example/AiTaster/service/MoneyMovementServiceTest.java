package com.example.AiTaster.service;

import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.repository.UserWalletRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoneyMovementServiceTest {

    @Mock
    private UserWalletRepo userWalletRepo;
    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private PlatformFeeCalculator platformFeeCalculator;
    @Mock
    private ProjectEscrowBalanceService projectEscrowBalanceService;

    @InjectMocks
    private MoneyMovementService moneyMovementService;

    @Test
    void calculateFeeCreatesAdminWalletWhenMissing() throws Exception {
        User admin = User.builder().userId(1L).username("admin").build();
        PaymentTransaction savedTransaction = PaymentTransaction.builder()
                .paymentTransactionId(99L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        AtomicLong walletIdSequence = new AtomicLong(10L);
        AtomicReference<UserWallet> savedWallet = new AtomicReference<>();

        setAdminUsername("admin");
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userWalletRepo.findByUserForUpdate(admin)).thenReturn(Optional.empty());
        when(platformFeeCalculator.calculatePlatformFee(BigDecimal.valueOf(10_000)))
                .thenReturn(BigDecimal.valueOf(1_000));
        when(userWalletRepo.findByUserIdForUpdate(1L)).thenAnswer(invocation ->
                Optional.ofNullable(savedWallet.get())
        );
        when(userWalletRepo.save(any(UserWallet.class))).thenAnswer(invocation -> {
            UserWallet wallet = invocation.getArgument(0);
            if (wallet.getUserWalletId() == null) {
                wallet.setUserWalletId(walletIdSequence.incrementAndGet());
            }
            savedWallet.set(wallet);
            return wallet;
        });
        when(paymentTransactionRepo.save(any(PaymentTransaction.class))).thenReturn(savedTransaction);

        BigDecimal expertAmount = moneyMovementService.calculateFee(BigDecimal.valueOf(10_000));

        assertThat(expertAmount).isEqualByComparingTo(BigDecimal.valueOf(9_000));
        assertThat(savedWallet.get()).isNotNull();
        assertThat(savedWallet.get().getUser().getUsername()).isEqualTo("admin");
        assertThat(savedWallet.get().getStatus()).isEqualTo(UserWalletStatus.ACTIVE);
        assertThat(savedWallet.get().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1_000));
    }

    @Test
    void moneyTransactionManagementCreatesRecipientWalletWhenMissing() throws Exception {
        User sender = User.builder().userId(10L).username("sender").build();
        User recipient = User.builder().userId(20L).username("recipient").build();
        PaymentTransaction savedTransaction = PaymentTransaction.builder()
                .paymentTransactionId(88L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        AtomicLong walletIdSequence = new AtomicLong(20L);
        AtomicReference<UserWallet> savedWallet = new AtomicReference<>();

        when(userRepo.findById(20L)).thenReturn(Optional.of(recipient));
        when(userWalletRepo.findByUserIdForUpdate(20L)).thenAnswer(invocation ->
                Optional.ofNullable(savedWallet.get())
        );
        when(userWalletRepo.save(any(UserWallet.class))).thenAnswer(invocation -> {
            UserWallet wallet = invocation.getArgument(0);
            if (wallet.getUserWalletId() == null) {
                wallet.setUserWalletId(walletIdSequence.incrementAndGet());
            }
            savedWallet.set(wallet);
            return wallet;
        });
        when(paymentTransactionRepo.save(any(PaymentTransaction.class))).thenReturn(savedTransaction);

        PaymentTransaction transaction = moneyMovementService.moneyTransactionManagement(
                sender.getUserId(),
                recipient.getUserId(),
                TransactionType.PROJECT_ESCROW_REFUND,
                30L,
                PaymentReferenceType.PROJECT,
                "Refund escrow",
                BigDecimal.valueOf(4_000),
                BigDecimal.valueOf(4_000),
                null
        );

        assertThat(transaction.getPaymentTransactionId()).isEqualTo(88L);
        assertThat(savedWallet.get()).isNotNull();
        assertThat(savedWallet.get().getUser().getUsername()).isEqualTo("recipient");
        assertThat(savedWallet.get().getStatus()).isEqualTo(UserWalletStatus.ACTIVE);
        assertThat(savedWallet.get().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(4_000));
    }

    private void setAdminUsername(String value) throws Exception {
        Field field = MoneyMovementService.class.getDeclaredField("adminUsername");
        field.setAccessible(true);
        field.set(moneyMovementService, value);
    }
}
