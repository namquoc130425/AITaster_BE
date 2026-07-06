package com.example.AiTaster.repository;

import com.example.AiTaster.constant.InvoiceType;
import com.example.AiTaster.entity.Invoices;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoicesRepo extends JpaRepository<Invoices, Long> {
    // Project invoice chỉ tạo sau khi escrow RELEASED.
    // Một project escrow chỉ có tối đa một hóa đơn
    Optional<Invoices> findByProjectEscrowId(Long projectEscrowId);

    // Dùng cho Aiserrvie và chống retry tạo trùng invoice.
    // Một payment thành công chỉ được sinh ra 1 hóa đơn
    Optional<Invoices> findByPaymentTransactionId(Long paymentTransactionId);

    List<Invoices> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Invoices> findByClientIdAndInvoiceTypeOrderByCreatedAtDesc(Long clientId, InvoiceType invoiceType
    );
}
