package com.example.AiTaster.repository;

import com.example.AiTaster.constant.InvoiceEmailRecipientRole;
import com.example.AiTaster.constant.InvoiceEmailStatus;
import com.example.AiTaster.constant.InvoiceEmailType;
import com.example.AiTaster.entity.InvoiceEmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceEmailLogRepo extends JpaRepository<InvoiceEmailLog, Long> {
    // Tìm email log theo khóa nghiệp vụ để chống tạo trùng khi webhook/service bị gọi lại.
    Optional<InvoiceEmailLog> findByInvoiceIdAndRecipientUserIdAndRecipientRoleAndEmailType(
            Long invoiceId,
            Long recipientUserId,
            InvoiceEmailRecipientRole recipientRole,
            InvoiceEmailType emailType
    );

    // Lấy các email log còn dưới giới hạn gửi để phục vụ retry thủ công hoặc scheduler sau này.
    List<InvoiceEmailLog> findByStatusAndSendAttemptCountLessThanOrderByCreatedAtAsc(
            InvoiceEmailStatus status,
            Integer maxSendAttempt
    );
}
