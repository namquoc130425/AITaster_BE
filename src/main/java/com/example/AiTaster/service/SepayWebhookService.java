package com.example.AiTaster.service;


import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.ProjectEscrow;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.InvitationRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SepayWebhookService {
    private static final Pattern PAYMENT_CODE_PATTERN = Pattern.compile("AIT-INV-\\d+-[A-Z0-9]{8}", Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter SEPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final InvitationRepo invitationRepo;

    @Value("${app.sepay.webhook-secret}")
    private String webhookSecret;

    private final ObjectMapper objectMapper;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final ProjectRepo projectRepo;
    private final ProjectEscrowRepo projectEscrowRepo;

    @Transactional
    public void handleWebhook(String rawBody,  String secretKey) {
        log.info("Received raw body: {}", rawBody);

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
        //khác kiểu NH , status v...
        if(!isInvitationSepayPayment(paymentTransaction)) {
            markFailed(paymentTransaction,request,providerTransactionCode);
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
        Invitation invitation = invitationRepo.findWithDetailByInvitationId(paymentTransaction.getReferenceId())
                .orElse(null);

        if (invitation == null || !InvitationStatus.ACCEPTED.equals(invitation.getInvitationStatus())) {
            markFailed(paymentTransaction, request, providerTransactionCode);
            return;
        }

        LocalDateTime paidAt = parsetransactionDate(request.getTransaction().getTransactionDate());

        Project newProject = createProjectByExpertAcceptInvitation(invitation,paymentTransaction);
        ProjectEscrow newProjectEscrow = createProjectEscrow(newProject);

        paymentTransaction.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentTransaction.setProviderTransactionCode(providerTransactionCode);
        paymentTransaction.setProviderContent(buildProviderContent(request));;
        paymentTransaction.setPaidAt(paidAt);
        paymentTransaction.setProjectEscrowId(newProjectEscrow.getProjectEscrowId());

        newProjectEscrow.setHeldAmount(paymentTransaction.getAmount());
        newProjectEscrow.setStartedAt(paidAt);

        newProject.setIsActive(true);
        newProject.setStartAt(paidAt);
        newProject.setDeadlineAt(setUpDeadlike(paidAt,newProject.getTimelineValue(),newProject.getTimelineUnit()));

        paymentTransactionRepo.save(paymentTransaction);
        projectEscrowRepo.save(newProjectEscrow);
        projectRepo.save(newProject);


    }

    //paseBody chuyển rawboy sang SePayRequest
    private SepayWebhookRequest parseBody(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, SepayWebhookRequest.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook body");
        }
    }

    /**
     * SỬA:
     * Cổng thanh toán SePay IPN dùng X-Secret-Key.
     * Secret này phải giống với app.sepay.webhook-secret trong application.yml.
     */
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
    /**
     * SỬA:
     * Webhook cũ tìm paymentCode trong code/content/description.
     * IPN mới chuẩn nhất là lấy order.order_invoice_number.
     *
     * Điều kiện:
     * Khi tạo đơn SePay, bạn phải set:
     * order_invoice_number = paymentTransaction.getPaymentCode()
     */
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

        Matcher matcher = PAYMENT_CODE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group().toUpperCase(Locale.ROOT);
    }

    private boolean isInvitationSepayPayment(PaymentTransaction payment) {
        return PaymentMethod.SEPAY.equals(payment.getPaymentMethod()) && PaymentReferenceType.INVITATION.equals(payment.getPaymentReferenceType());
    }

    private boolean isExpired(PaymentTransaction payment) {
        return payment.getExpiredAt() != null && payment.getExpiredAt().isBefore(LocalDateTime.now());
    }

    private boolean isAmountMatched(PaymentTransaction payment, BigDecimal transferAmount) {
        return transferAmount != null && payment.getAmount() != null && payment.getAmount().compareTo(transferAmount) == 0;
    }

    private ProjectEscrow createProjectEscrow(Project project) {
        if (projectEscrowRepo.existsByProjectId(project.getProjectId())) {
            throw new GlobalException(400, "Project escrow already exists");
        }

        Long clientProfileId = project.getInvitation()
                .getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getClientProfileId();

        Long expertProfileId = project.getInvitation()
                .getExpertApplication()
                .getExpertProfile()
                .getExpertProfileId();

        ProjectEscrow escrow = ProjectEscrow.builder()
                .projectId(project.getProjectId())
                .clientProfileId(clientProfileId)
                .expertProfileId(expertProfileId)
                .agreedAmount(project.getAgreedPrice())
                .heldAmount(BigDecimal.ZERO)
                .platformFee(BigDecimal.ZERO)
                .expertAmount(project.getAgreedPrice())
                .escrowStatus(EscrowStatus.HELD)
                .startedAt(null)
                .build();

        return projectEscrowRepo.save(escrow);
    }


    private Project createProjectByExpertAcceptInvitation(Invitation invitation, PaymentTransaction payment) {
        if (projectRepo.existsByInvitation(invitation)) {
            throw new GlobalException(400, "Project already exists for this invitation");
        }
        Project project = Project.builder()
                .invitation(invitation)
                .title(invitation.getProjectTitle())
                .finalRequirementSnapshot(invitation.getFinalRequirement())
                .expectedOutputSnapshot(invitation.getExpectedOutput())
                .acceptanceCriteriaSnapshot(invitation.getAcceptanceCriteria())
                .agreedPrice(invitation.getFinalOfferedPrice())
                .timeline(invitation.getFinalTimeline())
                .timelineValue(invitation.getFinalTimelineValue())
                .timelineUnit(invitation.getFinalTimelineUnit())
                .deadlineAt(null)
                .startAt(null)
                .completedAt(null)
                .paymentDeadlineAt(payment.getExpiredAt())
                .projectStatus(ProjectStatus.ACTIVE)
                .isActive(false)
                .build();
        return projectRepo.save(project);
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

        invitationRepo.findByInvitationId(paymentTransaction.getReferenceId()).ifPresent(
                invitation -> {
                    invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED);
                    invitationRepo.save(invitation);
                });
    }

    // hàm này setupDeadline
    private LocalDateTime setUpDeadlike(LocalDateTime paidAt, Integer value, TimelineUnit timelineUnit) {
        TimelineUnit unit;
        if (paidAt == null || value == null || timelineUnit == null) {
            return null;
        }

        return switch (timelineUnit) {
            case DAY -> paidAt.plusDays(value);
            case WEEK -> paidAt.plusWeeks(value);
            case MONTH -> paidAt.plusMonths(value);
        };
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

    //dữ liệu được gữi về -> kí lại dữ liệu
    private String hmacSha256Base64(String rawData, String secret) {
        try {
            // Tạo bộ ký HMAC-SHA256.
            Mac mac = Mac.getInstance("HmacSHA256");

            // Nạp secret key vào bộ ký.
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            // Ký rawData.
            byte[] digest = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));

            // SePay yêu cầu encode kết quả HMAC sang Base64.
            return Base64.getEncoder().encodeToString(digest);

        } catch (Exception e) {
            throw new GlobalException(500, "Cannot sign SePay checkout form");
        }
    }

    //So sánh 2 chữ ký
    private boolean secureEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }

    //chỉnh lại signature thành viết thường và xóa chứ sha256 ở đầu chuổi
    private String nnormalizeSignature(String signature) {
        return signature.trim().replaceFirst("(?i)^sha256=", "");
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
