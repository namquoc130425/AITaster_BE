package com.example.AiTaster.controller;

import com.example.AiTaster.constant.InvoiceEmailRecipientRole;
import com.example.AiTaster.constant.InvoiceEmailType;
import com.example.AiTaster.constant.InvoiceStatus;
import com.example.AiTaster.constant.InvoiceType;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/local-test/invoice-email")
@CrossOrigin("*")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.invoice-email-test", name = "enabled", havingValue = "true")
public class LocalInvoiceEmailTestController {
    private static final String CLIENT_EMAIL = "diennvtse184561@fpt.edu.vn";
    private static final String EXPERT_EMAIL = "ngodien2905@gmail.com";

    private final EmailService emailService;

    // Gửi thử email invoice mua AIService cho client bằng dữ liệu giả lập local.
    @PostMapping("/ai-service")
    public ResponseEntity<Map<String, Object>> sendAiServiceInvoice(
            @RequestBody LocalInvoiceEmailTestRequest request
    ) {
        Invoices invoice = buildInvoice(
                request,
                InvoiceType.SERVICE_ORDER,
                "Thanh toán AIService local test: " + safeText(request.serviceName(), "AIService")
        );

        emailService.sendInvoiceEmail(
                CLIENT_EMAIL,
                InvoiceEmailRecipientRole.CLIENT,
                InvoiceEmailType.SERVICE_ORDER_PAID,
                invoice,
                "Client Local Test",
                safeText(request.serviceName(), "AIService Local Test"),
                null
        );

        return successResponse("AIService invoice email sent", CLIENT_EMAIL, null, invoice);
    }

    // Gửi thử email invoice hoàn thành project cho cả client và expert bằng dữ liệu giả lập local.
    @PostMapping("/project-completion")
    public ResponseEntity<Map<String, Object>> sendProjectCompletionInvoice(
            @RequestBody LocalInvoiceEmailTestRequest request
    ) {
        Invoices invoice = buildInvoice(
                request,
                InvoiceType.PROJECT_COMPLETION,
                "Thanh toán project local test: " + safeText(request.projectTitle(), "AI Project")
        );

        String projectTitle = safeText(request.projectTitle(), "AI Project Local Test");

        emailService.sendInvoiceEmail(
                CLIENT_EMAIL,
                InvoiceEmailRecipientRole.CLIENT,
                InvoiceEmailType.PROJECT_COMPLETION_PAID,
                invoice,
                "Client Local Test",
                null,
                projectTitle
        );
        emailService.sendInvoiceEmail(
                EXPERT_EMAIL,
                InvoiceEmailRecipientRole.EXPERT,
                InvoiceEmailType.PROJECT_COMPLETION_PAID,
                invoice,
                "Expert Local Test",
                null,
                projectTitle
        );

        return successResponse("Project completion invoice emails sent", CLIENT_EMAIL, EXPERT_EMAIL, invoice);
    }

    // Dựng invoice giả lập đủ field cần cho template email invoice.
    private Invoices buildInvoice(
            LocalInvoiceEmailTestRequest request,
            InvoiceType invoiceType,
            String description
    ) {
        BigDecimal amount = request.amount() != null && request.amount().compareTo(BigDecimal.ZERO) > 0
                ? request.amount()
                : BigDecimal.valueOf(1_000_000);
        BigDecimal platformFee = amount.multiply(BigDecimal.valueOf(0.1));

        Invoices invoice = new Invoices();
        invoice.setInvoiceCode(safeText(request.invoiceCode(), defaultInvoiceCode(invoiceType)));
        invoice.setClientId(1L);
        invoice.setExpertId(2L);
        invoice.setInvoiceType(invoiceType);
        invoice.setInvoiceStatus(InvoiceStatus.PAID);
        invoice.setSubtotalAmount(amount);
        invoice.setPlatformFee(platformFee);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(amount);
        invoice.setCurrency("VND");
        invoice.setPaymentMethod("LOCAL_TEST");
        invoice.setDescription(description);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setCreatedAt(LocalDateTime.now());
        return invoice;
    }

    // Trả về response gọn để HTML local hiển thị kết quả gửi email.
    private ResponseEntity<Map<String, Object>> successResponse(
            String message,
            String clientEmail,
            String expertEmail,
            Invoices invoice
    ) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "invoiceCode", invoice.getInvoiceCode(),
                "clientEmail", clientEmail,
                "expertEmail", expertEmail == null ? "" : expertEmail
        ));
    }

    // Tạo mã invoice mặc định theo loại invoice nếu HTML không truyền lên.
    private String defaultInvoiceCode(InvoiceType invoiceType) {
        String prefix = InvoiceType.PROJECT_COMPLETION.equals(invoiceType)
                ? "LOCAL-PROJECT"
                : "LOCAL-AISERVICE";
        return prefix + "-" + System.currentTimeMillis();
    }

    // Chuẩn hóa chuỗi rỗng về fallback để template không bị thiếu dữ liệu.
    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public record LocalInvoiceEmailTestRequest(
            String invoiceCode,
            String serviceName,
            String projectTitle,
            BigDecimal amount
    ) {
    }
}
