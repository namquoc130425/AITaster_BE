package com.example.AiTaster.service;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendingPaymentServiceTest {

    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private PendingPaymentService pendingPaymentService;

    @Test
    void cancelPendingPayment_marksOwnedSepayPaymentCanceled() {
        User currentUser = User.builder().userId(10L).build();
        PaymentTransaction payment = PaymentTransaction.builder()
                .senderId(10L)
                .paymentMethod(PaymentMethod.SEPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentCode("AIT-PAY-1-ABC12345")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(paymentTransactionRepo.findByPaymentCode("AIT-PAY-1-ABC12345"))
                .thenReturn(Optional.of(payment));

        pendingPaymentService.cancelPendingPayment("ait-pay-1-abc12345");

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        verify(paymentTransactionRepo).save(payment);
    }

    @Test
    void cancelPendingPayment_rejectsAnotherUsersPayment() {
        User currentUser = User.builder().userId(10L).build();
        PaymentTransaction payment = PaymentTransaction.builder()
                .senderId(20L)
                .paymentMethod(PaymentMethod.SEPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentCode("AIT-PAY-1-ABC12345")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(paymentTransactionRepo.findByPaymentCode("AIT-PAY-1-ABC12345"))
                .thenReturn(Optional.of(payment));

        assertThatThrownBy(
                () -> pendingPaymentService.cancelPendingPayment("AIT-PAY-1-ABC12345")
        )
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("không có quyền");

        verify(paymentTransactionRepo, never()).save(payment);
    }

    @Test
    void cancelPendingPayment_isIdempotentWhenAlreadyCanceled() {
        User currentUser = User.builder().userId(10L).build();
        PaymentTransaction payment = PaymentTransaction.builder()
                .senderId(10L)
                .paymentMethod(PaymentMethod.SEPAY)
                .paymentStatus(PaymentStatus.CANCELED)
                .paymentCode("AIT-PAY-1-ABC12345")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(paymentTransactionRepo.findByPaymentCode("AIT-PAY-1-ABC12345"))
                .thenReturn(Optional.of(payment));

        pendingPaymentService.cancelPendingPayment("AIT-PAY-1-ABC12345");

        verify(paymentTransactionRepo, never()).save(payment);
    }
}
