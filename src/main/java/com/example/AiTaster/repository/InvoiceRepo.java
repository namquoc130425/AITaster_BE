package com.example.AiTaster.repository;

import com.example.AiTaster.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepo extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceId(Long invoiceId);

    List<Invoice> findByClientIdOrderByCreateAtDesc(Long clientId);
}
