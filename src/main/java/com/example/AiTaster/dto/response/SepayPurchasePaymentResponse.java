package com.example.AiTaster.dto.response;
import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SepayPurchasePaymentResponse {
    Long paymentTransactionId;
    Long senderId;
    Long receiverId;

    Long referenceId;
    PaymentReferenceType paymentReferenceType;
    TransactionType transactionType;

    BigDecimal amount;
    String currency;

    PaymentMethod paymentMethod;
    PaymentStatus paymentStatus;

    String paymentCode;
    String providerName;

    SepayCheckoutFormResponse checkoutForm;

    LocalDateTime expiredAt;
    LocalDateTime createAt;
}
