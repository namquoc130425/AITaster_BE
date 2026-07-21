package com.example.AiTaster.entity;

import com.example.AiTaster.constant.InvoiceStatus;
import com.example.AiTaster.constant.InvoiceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "invoices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invoice_code", columnNames = "invoice_code"),
                @UniqueConstraint(name = "uk_invoice_project_escrow", columnNames = "project_escrow_id"),
                @UniqueConstraint(name = "uk_invoice_payment_transaction", columnNames = "payment_transaction_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @Column(name = "invoice_code", nullable = false, unique = true)
    private String invoiceCode;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "expert_id")
    private Long expertId;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 50, columnDefinition = "varchar(50)")
    private InvoiceType invoiceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false)
    private InvoiceStatus invoiceStatus;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_escrow_id")
    private Long projectEscrowId;

    // Hien tai du an chua co ServiceOrder entity.
    // Field nay de san neu sau nay tach moi lan mua AI Service thanh order rieng.
    @Column(name = "service_order_id")
    private Long serviceOrderId;

    @Column(name = "payment_transaction_id")
    private Long paymentTransactionId;

    @Column(nullable = false)
    private BigDecimal subtotalAmount;

    @Column(nullable = false)
    private BigDecimal platformFee;

    @Column(nullable = false)
    private BigDecimal taxAmount;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String currency;

    private String paymentMethod;

    private String description;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
}
