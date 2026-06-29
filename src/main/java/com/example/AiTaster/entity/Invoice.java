package com.example.AiTaster.entity;

import com.example.AiTaster.constant.InvoiceStatus;
import com.example.AiTaster.constant.InvoiceType;
import com.example.AiTaster.constant.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long invoiceId;

    Long projectId;
    Long projectEscrowId;
    Long clientId;
    Long expertId;
    Long paymentTransactionId;

    @Column(nullable = false, unique = true)
    String invoiceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    InvoiceType invoiceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    InvoiceStatus invoiceStatus;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal subtotalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal platformFee;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal taxAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal discountAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal totalAmount;

    @Column(nullable = false, length = 10)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    PaymentMethod paymentMethod;

    @Column(columnDefinition = "TEXT")
    String description;

    LocalDateTime paidAt;
    LocalDateTime cancelledAt;

    @CreationTimestamp
    LocalDateTime createAt;

    @PrePersist
    public void prePersist() {
        if (invoiceStatus == null) invoiceStatus = InvoiceStatus.PAID;
        if (currency == null) currency = "VND";
        if (paymentMethod == null) paymentMethod = PaymentMethod.WALLET;
        if (platformFee == null) platformFee = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
    }
}
