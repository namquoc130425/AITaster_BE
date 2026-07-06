package com.example.AiTaster.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceResponse {
    Long invoiceId;
    String invoiceCode;
    Long clientId;
    Long expertId;
    String invoiceType;
    String invoiceStatus;
    Long projectId;
    Long projectEscrowId;
    Long serviceOrderId;
    Long paymentTransactionId;
    BigDecimal subtotalAmount;
    BigDecimal platformFee;
    BigDecimal taxAmount;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    String currency;
    String paymentMethod;
    String description;
    LocalDateTime paidAt;
    LocalDateTime createAt;
    LocalDateTime createdAt;
}
