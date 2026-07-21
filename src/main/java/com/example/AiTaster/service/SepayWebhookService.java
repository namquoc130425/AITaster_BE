package com.example.AiTaster.service;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.repository.InvitationRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
<<<<<<< HEAD
=======
import com.example.AiTaster.repository.ProjectRepo;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import com.example.AiTaster.service.payment.SepayPaymentHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SepayWebhookService {
    private static final Pattern PAYMENT_CODE_PATTERN =
            Pattern.compile("AIT-INV-\\d+-[A-Z0-9]{8}", Pattern.CASE_INSENSITIVE);

    private static final Pattern WALLET_DEPOSIT_PAYMENT_CODE_PATTERN =
            Pattern.compile("AIT-WALLET-IN-\\d+-[A-Z0-9]{8}", Pattern.CASE_INSENSITIVE);

    private static final Pattern GENERIC_PAYMENT_CODE_PATTERN =
            Pattern.compile("AIT-PAY-\\d+-[A-Z0-9]{8}", Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter SEPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InvitationRepo invitationRepo;
<<<<<<< HEAD
=======
    private final ProjectRepo projectRepo;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

    @Value("${app.sepay.webhook-secret}")
    private String webhookSecret;

    private final ObjectMapper objectMapper;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final List<SepayPaymentHandler> sepayPaymentHandler;

    @Transactional
    public void handleWebhook(String rawBody, String secretKey) {
        verifySecretKey(secretKey);

        SepayWebhookRequest request = parseBody(rawBody);

        // Chỉ nhận IPN Payment Gateway khi đủ ORDER_PAID + CAPTURED + APPROVED.
        if (!isPaidIpn(request)) {
            return;
        }

        // Payload IPN mới dùng transaction.transaction_id.
        String providerTransactionCode = buildProviderTransactionCode(request);
        if (providerTransactionCode == null) {
            return;
        }

        // Bỏ qua webhook trùng đã xử lý trước đó.
        if (paymentTransactionRepo.findByProviderTransactionCode(providerTransactionCode).isPresent()) {
            return;
        }

        // Ưu tiên order.order_invoice_number vì đây là paymentCode của hệ thống.
        String paymentCode = extractPaymentCode(request);
        if (paymentCode == null) {
            return;
        }

        PaymentTransaction paymentTransaction =
                paymentTransactionRepo.findByPaymentCode(paymentCode).orElse(null);

        if (paymentTransaction == null
                || !PaymentStatus.PENDING.equals(paymentTransaction.getPaymentStatus())) {
            return;
        }

        if (isExpired(paymentTransaction)) {
            checkPaymentExpired(paymentTransaction);
            return;
        }

        // Payload IPN mới dùng amount từ transaction.
        BigDecimal paidAmount = request.getTransaction().getTransactionAmount();

        if (!isAmountMatched(paymentTransaction, paidAmount)) {
            markFailed(paymentTransaction, request, providerTransactionCode);
            return;
        }

        LocalDateTime paidAt = parseTransactionDate(
                request.getTransaction().getTransactionDate()
        );

        String providerContent = buildProviderContent(request);

        SepayPaymentHandler paymentHandler = sepayPaymentHandler.stream()
                .filter(handler -> handler.supports(paymentTransaction))
                .findFirst()
                .orElse(null);

        if (paymentHandler == null) {
            markFailed(paymentTransaction, request, providerTransactionCode);
            return;
        }

        paymentHandler.handle(
                paymentTransaction,
                request,
                providerTransactionCode,
                providerContent,
                paidAt
        );
    }

    // Parse raw webhook body sang DTO yêu cầu của SePay.
    private SepayWebhookRequest parseBody(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, SepayWebhookRequest.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook body");
        }
    }

    // IPN Payment Gateway của SePay dùng header X-Secret-Key.
    private void verifySecretKey(String secretKey) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return;
        }

        if (secretKey == null || secretKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing SePay secret key");
        }

        if (!webhookSecret.equals(secretKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid SePay secret key");
        }
    }

    // IPN Payment Gateway mới được validate từ object order và transaction.
    private boolean isPaidIpn(SepayWebhookRequest request) {
        if (request == null || request.getOrder() == null || request.getTransaction() == null) {
            return false;
        }

        return "ORDER_PAID".equalsIgnoreCase(request.getNotificationType())
                && "CAPTURED".equalsIgnoreCase(request.getOrder().getOrderStatus())
                && "PAYMENT".equalsIgnoreCase(request.getTransaction().getTransactionType())
                && "APPROVED".equalsIgnoreCase(request.getTransaction().getTransactionStatus())
                && "VND".equalsIgnoreCase(request.getOrder().getOrderCurrency())
                && "VND".equalsIgnoreCase(request.getTransaction().getTransactionCurrency());
    }

    // Khi tạo đơn SePay, order_invoice_number phải bằng paymentCode.
    private String extractPaymentCode(SepayWebhookRequest sepayWebhookRequest) {
        if (sepayWebhookRequest == null || sepayWebhookRequest.getOrder() == null) {
            return null;
        }

        String invoiceNumber = sepayWebhookRequest.getOrder().getOrderInvoiceNumber();

        if (invoiceNumber != null && !invoiceNumber.isBlank()) {
            return invoiceNumber.trim().toUpperCase(Locale.ROOT);
        }

        // Dự phòng khi invoiceNumber bị thiếu.
        String text = nullToEmpty(sepayWebhookRequest.getOrder().getOrderDescription());

        String findPaymentCode = findPaymentCode(text, PAYMENT_CODE_PATTERN);
        if (findPaymentCode != null) {
            return findPaymentCode;
        }

        findPaymentCode = findPaymentCode(text, WALLET_DEPOSIT_PAYMENT_CODE_PATTERN);
        if (findPaymentCode != null) {
            return findPaymentCode;
        }

        findPaymentCode = findPaymentCode(text, GENERIC_PAYMENT_CODE_PATTERN);
        if (findPaymentCode != null) {
            return findPaymentCode;
        }

        return null;
    }

    private String findPaymentCode(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group().toUpperCase(Locale.ROOT);
    }

    private boolean isExpired(PaymentTransaction payment) {
        return payment.getExpiredAt() != null
                && payment.getExpiredAt().isBefore(LocalDateTime.now());
    }

    private boolean isAmountMatched(PaymentTransaction payment, BigDecimal transferAmount) {
        return transferAmount != null
                && payment.getGrossAmount() != null
                && payment.getGrossAmount().compareTo(transferAmount) == 0;
    }

    // Đánh dấu payment thất bại và giữ thông tin provider để audit/debug.
    private void markFailed(
            PaymentTransaction paymentTransaction,
            SepayWebhookRequest sepayWebhookRequest,
            String providerTransactionCode
    ) {
        paymentTransaction.setPaymentStatus(PaymentStatus.FAILED);
        paymentTransaction.setProviderTransactionCode(providerTransactionCode);
        paymentTransaction.setProviderContent(buildProviderContent(sepayWebhookRequest));
        paymentTransactionRepo.save(paymentTransaction);
    }

    // Đánh dấu payment hết hạn và cập nhật invitation nếu cần.
    private void checkPaymentExpired(PaymentTransaction paymentTransaction) {
        paymentTransaction.setPaymentStatus(PaymentStatus.EXPIRED);
        paymentTransactionRepo.save(paymentTransaction);

        if (PaymentReferenceType.INVITATION.equals(paymentTransaction.getPaymentReferenceType())) {
            invitationRepo.findByInvitationId(paymentTransaction.getReferenceId())
                    .ifPresent(invitation -> {
<<<<<<< HEAD
                        invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED);
                        invitationRepo.save(invitation);
=======
                        if (InvitationStatus.ACCEPTED.equals(invitation.getInvitationStatus())
                                && !projectRepo.existsByInvitation(invitation)) {
                            invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED);
                            invitationRepo.save(invitation);
                        }
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
                    });
        }
    }

    // Parse ngày giao dịch SePay. Nếu thiếu hoặc sai format thì dùng thời gian hiện tại.
    private LocalDateTime parseTransactionDate(String transactionDate) {
        if (transactionDate == null || transactionDate.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(transactionDate, SEPAY_DATE_FORMAT);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String buildProviderTransactionCode(SepayWebhookRequest sepayWebhookRequest) {
        if (sepayWebhookRequest == null || sepayWebhookRequest.getTransaction() == null) {
            return null;
        }

        return firstNotBlank(
                sepayWebhookRequest.getTransaction().getTransactionId(),
                sepayWebhookRequest.getTransaction().getId()
        );
    }

    private String buildProviderContent(SepayWebhookRequest sepayWebhookRequest) {
        if (sepayWebhookRequest == null || sepayWebhookRequest.getOrder() == null) {
            return null;
        }

        return firstNotBlank(
                sepayWebhookRequest.getOrder().getOrderDescription(),
                sepayWebhookRequest.getOrder().getOrderInvoiceNumber(),
                sepayWebhookRequest.getOrder().getOrderId()
        );
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
