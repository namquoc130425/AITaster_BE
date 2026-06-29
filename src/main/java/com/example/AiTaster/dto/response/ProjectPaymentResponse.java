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
public class ProjectPaymentResponse {
    Long paymentTransactionId;

    Long invitationId;

    Long senderId;

    BigDecimal amount;

    BigDecimal fromAmount;

    BigDecimal receiveAmount;

    String currency;

    TransactionType transactionType;

    PaymentMethod paymentMethod;

    PaymentStatus paymentStatus;

    PaymentReferenceType paymentReferenceType;

    Long referenceId;

    String providerName;

    String description;

    SepayCheckoutFormResponse checkoutForm;

    LocalDateTime expiredAt;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
