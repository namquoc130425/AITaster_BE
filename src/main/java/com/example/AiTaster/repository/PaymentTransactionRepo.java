package com.example.AiTaster.repository;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentTransactionRepo extends JpaRepository<PaymentTransaction, Long> {
    //SEpay webhook lấy paymentcode trong nội dung chuyển khoản ( trong content )
    // hàm này để tìm paymenttransaction cần update từ content (paymentcode) có trùng không
    Optional<PaymentTransaction> findByPaymentCode(String paymentCode);

    //mã giao dịch của SEpay gữi vê để chống webhook xũ lý hai lần
    Optional<PaymentTransaction>  findByProviderTransactionCode(String providerTransactionCode);;

    // Tìm payment PENDING của một object.
    // Phase project payment:
    // paymentReferenceType = PROJECT
    // referenceId = projectId
    // paymentStatus = PENDING
    //paymentMethod = SEPAY
    @Query("""
SELECT pt 
FROM PaymentTransaction pt
WHERE pt.paymentReferenceType = :paymentReferenceType
      AND pt.referenceId = :referenceId
      AND pt.paymentStatus = :paymentStatus
      AND pt.paymentMethod = :paymentMethod
""")
    Optional<PaymentTransaction> findPendingTransactionByReferenceAndMethod(
           @Param("paymentReferenceType") PaymentReferenceType paymentReferenceType,
          @Param("referenceId")  Long referenceId,
           @Param("paymentStatus")   PaymentStatus paymentStatus,
            @Param("paymentMethod") PaymentMethod paymentMethod
    );
}

