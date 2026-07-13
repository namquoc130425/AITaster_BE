package com.example.AiTaster.entity;

import com.example.AiTaster.constant.InvoiceEmailRecipientRole;
import com.example.AiTaster.constant.InvoiceEmailStatus;
import com.example.AiTaster.constant.InvoiceEmailType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "invoice_email_logs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_invoice_email_recipient",
                        columnNames = {
                                "invoice_id",
                                "recipient_user_id",
                                "recipient_role",
                                "email_type"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceEmailLogId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_role", nullable = false)
    private InvoiceEmailRecipientRole recipientRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false)
    private InvoiceEmailType emailType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceEmailStatus status;

    @Column(name = "send_attempt_count", nullable = false)
    private Integer sendAttemptCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Khởi tạo giá trị mặc định cho email log mới trước khi lưu lần đầu.
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (status == null) {
            status = InvoiceEmailStatus.PENDING;
        }
        if (sendAttemptCount == null) {
            sendAttemptCount = 0;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    // Cập nhật thời điểm thay đổi gần nhất cho email log.
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
