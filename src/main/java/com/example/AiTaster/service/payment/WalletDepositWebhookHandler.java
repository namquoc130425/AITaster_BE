package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.UserWalletRepo;
import com.example.AiTaster.service.MoneyMovementService;
import com.example.AiTaster.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WalletDepositWebhookHandler implements SepayPaymentHandler {
    private final UserWalletRepo userWalletRepo;

    private final PaymentTransactionRepo paymentTransactionRepo;
    private final MoneyMovementService moneyMovementService;
    private final RealtimeService realtimeService;


    @Override
    public boolean supports(PaymentTransaction payment) {

        return PaymentMethod.SEPAY.equals(payment.getPaymentMethod()) && PaymentReferenceType.USER_WALLET.equals(payment.getPaymentReferenceType()) && TransactionType.USER_DEPOSIT.equals(payment.getTransactionType());
    }

    @Override
    public void handle(
            PaymentTransaction payment,
            SepayWebhookRequest request,
            String providerTransactionCode,
            String providerContent,
            LocalDateTime paidAt
    ) {
        UserWallet wallet = userWalletRepo.findByUserWalletId(payment.getReferenceId()).orElseThrow(() -> new RuntimeException("UserWallet not found"));

        if (wallet == null || !UserWalletStatus.ACTIVE.equals(wallet.getStatus())) {
            markFailed(payment, request, providerTransactionCode);
            return;
        }
        PaymentTransaction successTransaction = moneyMovementService.moneyTransactionManagement(
                null,
                wallet.getUser().getUserId(),
                TransactionType.USER_DEPOSIT,
                wallet.getUserWalletId(),
                PaymentReferenceType.USER_WALLET,
                "SePay wallet deposit",
                BigDecimal.ZERO,
                payment.getGrossAmount(),
                payment.getPaymentTransactionId()
        );
        successTransaction.setProviderTransactionCode(providerTransactionCode);
        successTransaction.setProviderContent(providerContent);
        successTransaction.setPaidAt(paidAt);

        paymentTransactionRepo.save(successTransaction);
        realtimeService.pushUserWalletEvent(
                wallet.getUser(),
                "WALLET_DEPOSIT_SUCCEEDED",
                wallet.getUserWalletId(),
                "Wallet deposit succeeded"
        );
    }


    private void markFailed(PaymentTransaction paymentTransaction, SepayWebhookRequest sepayWebhookRequest, String providerTransactionCode) {
        paymentTransaction.setPaymentStatus(PaymentStatus.FAILED);
        paymentTransaction.setProviderTransactionCode(providerTransactionCode);
        paymentTransaction.setProviderContent(buildProviderContent(sepayWebhookRequest));
        paymentTransactionRepo.save(paymentTransaction);
    }

    private String buildProviderContent(SepayWebhookRequest sepayWebhookRequest) {
        if (sepayWebhookRequest == null || sepayWebhookRequest.getOrder() == null) {
            return null;
        }

        return firstNotBlank(
                sepayWebhookRequest.getOrder().getOrderDescription(),
                sepayWebhookRequest.getOrder().getOrderInvoiceNumber(),
                sepayWebhookRequest.getOrder().getOrderId()
        );


    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

}
