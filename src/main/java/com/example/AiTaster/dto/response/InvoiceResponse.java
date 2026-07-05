package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.InvoiceStatus;
import com.example.AiTaster.constant.InvoiceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceResponse {
    Long invoiceId;
    String invoiceCode;
    Long clientId;
    Long expertId;
    InvoiceType invoiceType;
    InvoiceStatus invoiceStatus;
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
    LocalDateTime createdAt;
}
