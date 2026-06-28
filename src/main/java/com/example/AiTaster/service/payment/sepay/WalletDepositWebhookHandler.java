package com.example.AiTaster.service.payment.sepay;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.UserWalletRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@Component
@RequiredArgsConstructor
public class WalletDepositWebhookHandler implements SepayPaymentHandler {
    private final UserWalletRepo  userWalletRepo;

    private final PaymentTransactionRepo paymentTransactionRepo;


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

        if(wallet == null || !UserWalletStatus.ACTIVE.equals(wallet.getStatus())) {
            markFailed(payment,request,providerTransactionCode);
            return;
        }
        wallet.setBalance(wallet.getBalance().add(payment.getAmount()));

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setProviderTransactionCode(providerTransactionCode)
        ;
        payment.setProviderContent(providerContent);
        payment.setPaidAt(paidAt);
        userWalletRepo.save(wallet);
        paymentTransactionRepo.save(payment);
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
