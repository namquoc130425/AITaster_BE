package com.example.AiTaster.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    Long invoiceId;
    String invoiceCode;
    String invoiceType;
    String invoiceStatus;
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
}
