package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentTransactionResponse {
    Long paymentTransactionId;
    Long projectEscrowId;
    Long expertServiceId;
    Long senderId;
    Long receiverId;
    Long sourceWalletId;
    Long targetWalletId;
    BigDecimal amount;
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
    LocalDateTime createAt;
    LocalDateTime updateAt;
}
