package com.example.AiTaster.service.payment;

import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.PaymentTransaction;

import java.time.LocalDateTime;

public interface SepayPaymentHandler {

    boolean supports(PaymentTransaction payment);

    void handle(
            PaymentTransaction payment,
            SepayWebhookRequest request,
            String providerTransactionCode,
            String providerContent,
            LocalDateTime paidAt
    );
}
