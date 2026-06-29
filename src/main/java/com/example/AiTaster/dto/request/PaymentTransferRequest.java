package com.example.AiTaster.dto.request;

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
public class PaymentTransferRequest {
    Long senderId;
    Long receiverId;
    Long sourceWalletId;
    Long targetWalletId;
    Long projectEscrowId;
    Long expertServiceId;
    BigDecimal fromAmount;
    BigDecimal receiveAmount;
    String currency;
    TransactionType transactionType;
    PaymentMethod paymentMethod;
    PaymentStatus paymentStatus;
    Long referenceId;
    PaymentReferenceType paymentReferenceType;
    String providerName;
    String providerTransactionCode;
    String paymentCode;
    String providerContent;
    String description;
    LocalDateTime paidAt;
    LocalDateTime expiredAt;
    Boolean debitSourceWallet;
    Boolean creditTargetWallet;
}
