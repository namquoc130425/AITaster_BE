package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.InvoiceStatus;
import com.example.AiTaster.constant.InvoiceType;
import com.example.AiTaster.constant.PaymentMethod;
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
public class InvoiceResponse {
    Long invoiceId;
    String invoiceCode;
    InvoiceType invoiceType;
    InvoiceStatus invoiceStatus;
    BigDecimal subtotalAmount;
    BigDecimal platformFee;
    BigDecimal taxAmount;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    String currency;
    PaymentMethod paymentMethod;
    String description;
    LocalDateTime paidAt;
    LocalDateTime createAt;
}
