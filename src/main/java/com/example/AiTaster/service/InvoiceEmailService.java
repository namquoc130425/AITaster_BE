package com.example.AiTaster.service;

import com.example.AiTaster.constant.InvoiceEmailRecipientRole;
import com.example.AiTaster.constant.InvoiceEmailStatus;
import com.example.AiTaster.constant.InvoiceEmailType;
import com.example.AiTaster.constant.InvoiceStatus;
import com.example.AiTaster.constant.InvoiceType;
import com.example.AiTaster.entity.InvoiceEmailLog;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.InvoiceEmailLogRepo;
import com.example.AiTaster.repository.InvoicesRepo;
import com.example.AiTaster.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceEmailService {
    public static final int MAX_SEND_ATTEMPT = 3;

    private final InvoicesRepo invoicesRepo;
    private final UserRepo userRepo;
    private final InvoiceEmailLogRepo invoiceEmailLogRepo;
    private final EmailService emailService;

    // Tạo các email log cần thiết cho invoice đã PAID, tùy loại invoice sẽ chọn recipient phù hợp.
    @Transactional
    public List<InvoiceEmailLog> enqueueForInvoice(Long invoiceId) {
        Invoices invoice = invoicesRepo.findById(invoiceId)
                .orElseThrow(() -> new GlobalException(404, "Invoice not found"));

        if (!InvoiceStatus.PAID.equals(invoice.getInvoiceStatus())) {
            throw new GlobalException(400, "Only paid invoices can be emailed");
        }

        List<InvoiceEmailLog> emailLogs = new ArrayList<>();

        if (InvoiceType.SERVICE_ORDER.equals(invoice.getInvoiceType())) {
            emailLogs.add(createPendingEmailIfMissing(
                    invoice,
                    invoice.getClientId(),
                    InvoiceEmailRecipientRole.CLIENT,
                    InvoiceEmailType.SERVICE_ORDER_PAID
            ));
            return emailLogs;
        }

        if (InvoiceType.PROJECT_COMPLETION.equals(invoice.getInvoiceType())
                || InvoiceType.DISPUTE_RESOLUTION.equals(invoice.getInvoiceType())) {
            emailLogs.add(createPendingEmailIfMissing(
                    invoice,
                    invoice.getClientId(),
                    InvoiceEmailRecipientRole.CLIENT,
                    InvoiceEmailType.PROJECT_COMPLETION_PAID
            ));
            emailLogs.add(createPendingEmailIfMissing(
                    invoice,
                    invoice.getExpertId(),
                    InvoiceEmailRecipientRole.EXPERT,
                    InvoiceEmailType.PROJECT_COMPLETION_PAID
            ));
        }

        return emailLogs;
    }

    // Tạo log email invoice rồi gửi ngay các log còn đủ điều kiện gửi tự động.
    @Transactional
    public void enqueueAndSendForInvoice(Long invoiceId) {
        List<InvoiceEmailLog> emailLogs = enqueueForInvoice(invoiceId);
        for (InvoiceEmailLog emailLog : emailLogs) {
            sendEmailLog(emailLog);
        }
    }

    // Gửi một email invoice theo log đã có, tăng số lần thử và cập nhật trạng thái gửi.
    @Transactional
    public InvoiceEmailLog sendEmailLog(Long invoiceEmailLogId) {
        InvoiceEmailLog emailLog = invoiceEmailLogRepo.findById(invoiceEmailLogId)
                .orElseThrow(() -> new GlobalException(404, "Invoice email log not found"));

        return sendEmailLog(emailLog);
    }

    // Kiểm tra email log còn được phép gửi tự động hay không.
    public boolean canAttemptSend(InvoiceEmailLog log) {
        if (log == null || InvoiceEmailStatus.SENT.equals(log.getStatus())) {
            return false;
        }
        return defaultZero(log.getSendAttemptCount()) < MAX_SEND_ATTEMPT;
    }

    // Tạo email log PENDING nếu chưa có log cùng invoice, recipient, role và loại email.
    private InvoiceEmailLog createPendingEmailIfMissing(
            Invoices invoice,
            Long recipientUserId,
            InvoiceEmailRecipientRole role,
            InvoiceEmailType emailType
    ) {
        if (recipientUserId == null) {
            return null;
        }

        User recipient = userRepo.findById(recipientUserId)
                .orElseThrow(() -> new GlobalException(404, "Invoice email recipient not found"));

        return invoiceEmailLogRepo
                .findByInvoiceIdAndRecipientUserIdAndRecipientRoleAndEmailType(
                        invoice.getInvoiceId(),
                        recipientUserId,
                        role,
                        emailType
                )
                .orElseGet(() -> invoiceEmailLogRepo.save(
                        InvoiceEmailLog.builder()
                                .invoiceId(invoice.getInvoiceId())
                                .recipientUserId(recipientUserId)
                                .recipientEmail(recipient.getEmail())
                                .recipientRole(role)
                                .emailType(emailType)
                                .status(InvoiceEmailStatus.PENDING)
                                .sendAttemptCount(0)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));
    }

    // Gửi email thật qua EmailService và lưu lại kết quả thành công hoặc thất bại.
    private InvoiceEmailLog sendEmailLog(InvoiceEmailLog emailLog) {
        if (!canAttemptSend(emailLog)) {
            return emailLog;
        }

        emailLog.setSendAttemptCount(defaultZero(emailLog.getSendAttemptCount()) + 1);
        emailLog.setUpdatedAt(LocalDateTime.now());

        try {
            Invoices invoice = invoicesRepo.findById(emailLog.getInvoiceId())
                    .orElseThrow(() -> new GlobalException(404, "Invoice not found"));
            User recipient = userRepo.findById(emailLog.getRecipientUserId())
                    .orElseThrow(() -> new GlobalException(404, "Invoice email recipient not found"));

            emailService.sendInvoiceEmail(
                    emailLog.getRecipientEmail(),
                    emailLog.getRecipientRole(),
                    emailLog.getEmailType(),
                    invoice,
                    resolveRecipientName(recipient),
                    resolveInvoiceDisplayName(invoice, "AI Service"),
                    resolveInvoiceDisplayName(invoice, "Dự án")
            );

            emailLog.setStatus(InvoiceEmailStatus.SENT);
            emailLog.setSentAt(LocalDateTime.now());
            emailLog.setLastError(null);
        } catch (Exception e) {
            emailLog.setStatus(InvoiceEmailStatus.FAILED);
            emailLog.setLastError(resolveErrorMessage(e));
        }

        return invoiceEmailLogRepo.save(emailLog);
    }

    // Lấy tên hiển thị của người nhận để đưa vào lời chào trong email.
    private String resolveRecipientName(User recipient) {
        if (recipient.getFullName() != null && !recipient.getFullName().isBlank()) {
            return recipient.getFullName();
        }

        if (recipient.getUsername() != null && !recipient.getUsername().isBlank()) {
            return recipient.getUsername();
        }

        return recipient.getEmail();
    }

    // Lấy mô tả invoice làm tên hiển thị tạm cho service hoặc project khi chưa có bảng chi tiết riêng.
    private String resolveInvoiceDisplayName(Invoices invoice, String fallback) {
        if (invoice.getDescription() != null && !invoice.getDescription().isBlank()) {
            return invoice.getDescription();
        }

        return fallback;
    }

    // Rút gọn message lỗi để lưu vào log gửi email invoice.
    private String resolveErrorMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    // Chuyển giá trị null về 0 để xử lý bộ đếm số lần gửi an toàn.
    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
