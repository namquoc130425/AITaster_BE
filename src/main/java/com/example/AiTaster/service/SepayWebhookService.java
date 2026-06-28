package com.example.AiTaster.service;
import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.repository.InvitationRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.service.payment.sepay.SepayPaymentHandler;
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
    private static final Pattern PAYMENT_CODE_PATTERN = Pattern.compile("AIT-INV-\\d+-[A-Z0-9]{8}", Pattern.CASE_INSENSITIVE);

    private static final Pattern WALLET_DEPOSIT_PAYMENT_CODE_PATTERN =
            Pattern.compile("AIT-WALLET-IN-\\d+-[A-Z0-9]{8}", Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter SEPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final InvitationRepo invitationRepo;

    @Value("${app.sepay.webhook-secret}")
    private String webhookSecret;

    private final ObjectMapper objectMapper;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final List<SepayPaymentHandler>  sepayPaymentHandler;

    @Transactional
    public void handleWebhook(String rawBody,  String secretKey) {

        verifySecretKey(secretKey);

        SepayWebhookRequest request = parseBody(rawBody);

        // IPN cổng thanh toán mới check ORDER_PAID + CAPTURED + APPROVED.
        if (!isPaidIpn(request)) {
            return;
        }


        // IPN mới lấy transaction.transaction_id.
        String providerTransactionCode = buildProviderTransactionCode(request);
        if (providerTransactionCode == null) {
            return;
        }
        // xuong db tìm mã code này đã có giao dịch chưa , có là đã giao dịch -> ko sữ lý lại
        if (paymentTransactionRepo.findByProviderTransactionCode(providerTransactionCode).isPresent()) {
            return;
        }

        // IPN mới ưu tiên order.order_invoice_number.
        String paymentCode = extractPaymentCode(request);
        if (paymentCode == null) {
            return;
        }

        PaymentTransaction paymentTransaction = paymentTransactionRepo.findByPaymentCode(paymentCode).orElse(null);

        if(paymentTransaction == null || !PaymentStatus.PENDING.equals(paymentTransaction.getPaymentStatus())) {
            return;
        }


        //quá hạn
        if (isExpired(paymentTransaction)) {
            checkPaymentExpired(paymentTransaction);
            return;
        }
        //tiền
        // IPN mới dùng request.getTransaction().getTransactionAmount().
        BigDecimal paidAmount = request.getTransaction().getTransactionAmount();

        if (!isAmountMatched(paymentTransaction, paidAmount)) {
            markFailed(paymentTransaction, request, providerTransactionCode);
            return;
        }

        LocalDateTime paidAt = parsetransactionDate(request.getTransaction().getTransactionDate());

        String providerContent = buildProviderContent(request);

        SepayPaymentHandler  paymentHandler = sepayPaymentHandler.stream()
                .filter(h -> h.supports(paymentTransaction)).findFirst().orElse(null);
        if(paymentHandler == null ) {
            markFailed(paymentTransaction, request, providerTransactionCode);
            return;
        }
        paymentHandler.handle(paymentTransaction,request,providerTransactionCode,providerContent,paidAt);
    }

    //paseBody chuyển rawboy sang SePayRequest
    private SepayWebhookRequest parseBody(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, SepayWebhookRequest.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook body");
        }
    }


    //  Cổng thanh toán SePay IPN dùng X-Secret-Key.

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


    // IPN cổng thanh toán mới check theo object order + transaction.
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

     // Điều kiện:
     // Khi tạo đơn SePay, phải set:
     // order_invoice_number = paymentTransaction.getPaymentCode()

    private String extractPaymentCode(SepayWebhookRequest sepayWebhookRequest) {
        if (sepayWebhookRequest == null || sepayWebhookRequest.getOrder() == null) {
            return null;
        }

        String invoiceNumber = sepayWebhookRequest.getOrder().getOrderInvoiceNumber();

        if (invoiceNumber != null && !invoiceNumber.isBlank()) {
            return invoiceNumber.trim().toUpperCase(Locale.ROOT);
        }

        // Fallback nếu invoiceNumber bị thiếu.
        String text = nullToEmpty(sepayWebhookRequest.getOrder().getOrderDescription());

        String findPaymentCode = findPaymentCode(text,PAYMENT_CODE_PATTERN);
        if (findPaymentCode != null) {
            return findPaymentCode;
        }
        findPaymentCode = findPaymentCode(text,WALLET_DEPOSIT_PAYMENT_CODE_PATTERN);
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
        return payment.getExpiredAt() != null && payment.getExpiredAt().isBefore(LocalDateTime.now());
    }

    private boolean isAmountMatched(PaymentTransaction payment, BigDecimal transferAmount) {
        return transferAmount != null && payment.getAmount() != null && payment.getAmount().compareTo(transferAmount) == 0;
    }

    // thanh toán thất bại set status payment , providerTransactionCode từ sepay gữi về ,ProviderContent từ lúc gữi đi cho sepay
    private void markFailed(PaymentTransaction paymentTransaction, SepayWebhookRequest sepayWebhookRequest, String providerTransactionCode) {
        paymentTransaction.setPaymentStatus(PaymentStatus.FAILED);
        paymentTransaction.setProviderTransactionCode(providerTransactionCode);
        paymentTransaction.setProviderContent(buildProviderContent(sepayWebhookRequest));;
        paymentTransactionRepo.save(paymentTransaction);
    }

    // thanh toán hết hạn thì đổi status sang Expired và invitation cũng đổi theo
    private void checkPaymentExpired(PaymentTransaction paymentTransaction) {
        paymentTransaction.setPaymentStatus(PaymentStatus.EXPIRED);
        paymentTransactionRepo.save(paymentTransaction);
        if (PaymentReferenceType.INVITATION.equals(paymentTransaction.getPaymentReferenceType())) {
            invitationRepo.findByInvitationId(paymentTransaction.getReferenceId()).ifPresent(
                    invitation -> {
                        invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED);
                        invitationRepo.save(invitation);

                    });
        }
    }



    //kiểm tra transactionDate mà sepay gữi về có hợp lệ không , nếu không lấy thời gian hiện tại . đúng format
    private LocalDateTime parsetransactionDate(String transactionDate) {
        if (transactionDate == null || transactionDate.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(transactionDate, SEPAY_DATE_FORMAT);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }


    /**
     * SỬA:
     * Webhook cũ lấy request.id hoặc referenceCode.
     * IPN mới lấy transaction.transaction_id.
     */
    private String buildProviderTransactionCode(SepayWebhookRequest sepayWebhookRequest) {
        if (sepayWebhookRequest == null || sepayWebhookRequest.getTransaction() == null) {
            return null;
        }

        return firstNotBlank(
                sepayWebhookRequest.getTransaction().getTransactionId(),
                sepayWebhookRequest.getTransaction().getId()
        );
    }

    /**
     * SỬA:
     * Webhook cũ có content/description ở root.
     * IPN mới có order.order_description.
     */
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





    // lấy chuổi đầu tiên không null và không rỗng
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
