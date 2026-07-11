package com.example.AiTaster.service;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.response.InvoiceResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.InvoiceMapper;
import com.example.AiTaster.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoicesRepo invoiceRepo;
    private final ProjectRepo projectRepo;
    private final ProjectEscrowRepo projectEscrowRepo;
    private final ExpertServiceRepo expertServiceRepo;
    private final InvoiceMapper invoiceMapper;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final CurrentUserService currentUserService;

    @Transactional
    public Invoices createForCompletedProject(Long projectId) {
        Project project = projectRepo.findWithDetailByProjectId(projectId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy dự án"));

        ProjectEscrow escrow = projectEscrowRepo.findByProjectId(projectId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy ký quỹ dự án"));

        if (!ProjectStatus.COMPLETED.equals(project.getProjectStatus())) {
            throw new GlobalException(400, "Dự án chưa hoàn thành");
        }

        if (!EscrowStatus.RELEASED.equals(escrow.getEscrowStatus())) {
            throw new GlobalException(400, "Tiền ký quỹ chưa được giải ngân");
        }

        return invoiceRepo.findByProjectEscrowId(escrow.getProjectEscrowId()).orElseGet(
                () -> {
                    // Project invoice tạo theo transaction RELEASE
                    List<PaymentTransaction> payments = paymentTransactionRepo
                            .findByPaymentReferenceTypeAndReferenceIdInAndTransactionTypeAndPaymentStatusAndPaymentMethod(
                                    PaymentReferenceType.PROJECT,
                                    List.of(project.getProjectId()),
                                    TransactionType.PROJECT_ESCROW_RELEASE,
                                    PaymentStatus.SUCCESS,
                                    PaymentMethod.WALLET
                            );
                    PaymentTransaction payment = payments.stream()
                            .findFirst()
                            .orElseThrow(() -> new GlobalException(404, "Không tìm thấy giao dịch giải ngân dự án"));
                    Invoices invoice = invoiceMapper.toProjectCompletionInvoice(project, escrow, payment);

                    invoice.setInvoiceCode(generateInvoiceCode());
                    invoice.setInvoiceType(InvoiceType.PROJECT_COMPLETION);

                    // paid khi project completed, escrow released, payment success.
                    invoice.setInvoiceStatus(InvoiceStatus.PAID);

                    BigDecimal totalAmount = defaultZero(payment.getGrossAmount());
                    BigDecimal expertAmount = defaultZero(payment.getNetAmount());
                    BigDecimal platformFee = totalAmount.subtract(expertAmount);

                    invoice.setSubtotalAmount(expertAmount);
                    invoice.setPlatformFee(platformFee);
                    invoice.setTaxAmount(BigDecimal.ZERO);
                    invoice.setDiscountAmount(BigDecimal.ZERO);
                    invoice.setTotalAmount(totalAmount);

                    invoice.setDescription("Hóa đơn cho dự án đã hoàn thành: " + project.getTitle());
                    invoice.setPaidAt(payment.getPaidAt());
                    invoice.setCreatedAt(LocalDateTime.now());

                    return invoiceRepo.save(invoice);

                });
    }

    @Transactional
    public Invoices createForPaidAiService(Long paymentTransactionId) {
        PaymentTransaction payment = paymentTransactionRepo.findById(paymentTransactionId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy giao dịch thanh toán"));

        if (!PaymentStatus.SUCCESS.equals(payment.getPaymentStatus())) {
            throw new GlobalException(400, "Thanh toán chưa thành công");
        }

        if (!TransactionType.EXPERT_SERVICE_PURCHASE.equals(payment.getTransactionType())
                || !PaymentReferenceType.EXPERT_SERVICE.equals(payment.getPaymentReferenceType())) {
            throw new GlobalException(400, "Thanh toán không phải giao dịch mua dịch vụ AI");
        }

        return invoiceRepo.findByPaymentTransactionId(paymentTransactionId)
                .orElseGet(() -> {
                    Long serviceId = payment.getExpertServiceId() != null
                            ? payment.getExpertServiceId()
                            : payment.getReferenceId();

                    ExpertService expertService = expertServiceRepo.findById(serviceId)
                            .orElseThrow(() -> new GlobalException(404, "Không tìm thấy dịch vụ chuyên gia"));

                    Invoices invoice = invoiceMapper.toAiServiceInvoice(expertService, payment);

                    invoice.setInvoiceCode(generateInvoiceCode());
                    invoice.setInvoiceType(InvoiceType.SERVICE_ORDER);

                    // AI Service la mua dich vu truc tiep.
                    // SEPAY hay WALLET deu tao invoice sau khi payment SUCCESS.
                    invoice.setInvoiceStatus(InvoiceStatus.PAID);

                    BigDecimal totalAmount = defaultZero(payment.getGrossAmount());
                    BigDecimal expertAmount = defaultZero(payment.getNetAmount());
                    BigDecimal platformFee = totalAmount.subtract(expertAmount);

                    invoice.setSubtotalAmount(expertAmount);
                    invoice.setPlatformFee(platformFee);
                    invoice.setTaxAmount(BigDecimal.ZERO);
                    invoice.setDiscountAmount(BigDecimal.ZERO);
                    invoice.setTotalAmount(totalAmount);

                    invoice.setDescription("Hóa đơn cho dịch vụ AI: " + expertService.getServiceName());
                    invoice.setPaidAt(payment.getPaidAt());
                    invoice.setCreatedAt(LocalDateTime.now());

                    return invoiceRepo.save(invoice);
                });
    }

    public List<InvoiceResponse> getMyInvoices(InvoiceType invoiceType) {
        User currentUser = currentUserService.getCurrentUser();

        List<Invoices> invoices = invoiceType == null
                ? invoiceRepo.findByClientIdOrderByCreatedAtDesc(currentUser.getUserId())
                : invoiceRepo.findByClientIdAndInvoiceTypeOrderByCreatedAtDesc(currentUser.getUserId(), invoiceType);

        return invoices.stream()
                .map(invoiceMapper::toInvoiceResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getMyInvoiceDetail(Long invoiceId) {
        User currentUser = currentUserService.getCurrentUser();

        Invoices invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy hóa đơn"));

        if (!currentUser.getUserId().equals(invoice.getClientId())) {
            throw new GlobalException(403, "Bạn không có quyền xem hóa đơn này");
        }

        return invoiceMapper.toInvoiceResponse(invoice);
    }

    private String generateInvoiceCode() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + date + "-" + random;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
