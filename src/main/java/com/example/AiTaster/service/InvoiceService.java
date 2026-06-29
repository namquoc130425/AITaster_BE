package com.example.AiTaster.service;

import com.example.AiTaster.constant.InvoiceStatus;
import com.example.AiTaster.constant.InvoiceType;
import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.dto.response.InvoiceResponse;
import com.example.AiTaster.entity.Invoice;
import com.example.AiTaster.repository.InvoiceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepo invoiceRepo;

    public Invoice createPaidInvoice(
            InvoiceType invoiceType,
            Long clientId,
            Long expertId,
            Long projectId,
            Long projectEscrowId,
            Long paymentTransactionId,
            BigDecimal subtotalAmount,
            BigDecimal platformFee,
            String description
    ) {
        BigDecimal safeSubtotal = subtotalAmount == null ? BigDecimal.ZERO : subtotalAmount;
        BigDecimal safePlatformFee = platformFee == null ? BigDecimal.ZERO : platformFee;

        Invoice invoice = Invoice.builder()
                .projectId(projectId)
                .projectEscrowId(projectEscrowId)
                .clientId(clientId)
                .expertId(expertId)
                .paymentTransactionId(paymentTransactionId)
                .invoiceCode(generateInvoiceCode(invoiceType))
                .invoiceType(invoiceType)
                .invoiceStatus(InvoiceStatus.PAID)
                .subtotalAmount(safeSubtotal)
                .platformFee(safePlatformFee)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(safeSubtotal)
                .currency("VND")
                .paymentMethod(PaymentMethod.WALLET)
                .description(description)
                .paidAt(LocalDateTime.now())
                .build();

        return invoiceRepo.save(invoice);
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .invoiceCode(invoice.getInvoiceCode())
                .invoiceType(invoice.getInvoiceType())
                .invoiceStatus(invoice.getInvoiceStatus())
                .subtotalAmount(invoice.getSubtotalAmount())
                .platformFee(invoice.getPlatformFee())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .currency(invoice.getCurrency())
                .paymentMethod(invoice.getPaymentMethod())
                .description(invoice.getDescription())
                .paidAt(invoice.getPaidAt())
                .createAt(invoice.getCreateAt())
                .build();
    }

    private String generateInvoiceCode(InvoiceType invoiceType) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return "INV-" + invoiceType.name() + "-" + random;
    }
}
