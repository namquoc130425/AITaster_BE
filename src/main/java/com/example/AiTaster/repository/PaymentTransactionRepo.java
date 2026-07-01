package com.example.AiTaster.repository;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.entity.PaymentTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepo extends JpaRepository<PaymentTransaction, Long> {
    //SEpay webhook lấy paymentcode trong nội dung chuyển khoản ( trong content )
    // hàm này để tìm paymenttransaction cần update từ content (paymentcode) có trùng không
    //Lấy PaymentTransaction có paymentCode bằng paymentCode truyền vào
    //@Lock: request A lấy được dòng payment này rồi, thì request B muốn lấy cùng dòng đó để xử lý sẽ phải chờ.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT pt
            FROM PaymentTransaction pt
            WHERE pt.paymentCode = :paymentCode
            """)
    Optional<PaymentTransaction> findByPaymentCode(String paymentCode);

    //mã giao dịch của SEpay gữi vê để chống webhook xũ lý hai lần
    Optional<PaymentTransaction>  findByProviderTransactionCode(String providerTransactionCode);;

    // Tìm payment PENDING của một object.
    // Luồng thanh toán project:
    // paymentReferenceType = INVITATION
    // referenceId = invitationId
    // paymentStatus = PENDING
    // paymentMethod = SEPAY
    @Query("""
SELECT pt 
FROM PaymentTransaction pt
WHERE pt.paymentReferenceType = :paymentReferenceType
      AND pt.referenceId = :referenceId
      AND pt.transactionType = :transactionType
      AND pt.senderId = :senderId
      AND pt.paymentStatus = :paymentStatus
      AND pt.paymentMethod = :paymentMethod
""")
    Optional<PaymentTransaction> findPendingTransactionByReferenceAndMethod(
           @Param("paymentReferenceType") PaymentReferenceType paymentReferenceType,
          @Param("referenceId")  Long referenceId,
          @Param("transactionType") TransactionType transactionType,
          @Param("senderId") Long senderId,
           @Param("paymentStatus")   PaymentStatus paymentStatus,
            @Param("paymentMethod") PaymentMethod paymentMethod
    );

    List<PaymentTransaction> findBySenderIdAndTransactionTypeAndPaymentReferenceTypeAndPaymentStatusOrderByCreateAtDesc(
            Long senderId,
            TransactionType transactionType,
            PaymentReferenceType paymentReferenceType,
            PaymentStatus paymentStatus
    );

    @Query("""
            SELECT pt
            FROM PaymentTransaction pt
            WHERE pt.senderId = :userId
               OR pt.receiverId = :userId
               OR pt.sourceWalletId = :walletId
               OR pt.targetWalletId = :walletId
            ORDER BY pt.createAt DESC
            """)
    List<PaymentTransaction> findMyWalletTransactions(
            @Param("userId") Long userId,
            @Param("walletId") Long walletId
    );
}

