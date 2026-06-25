package com.example.AiTaster.service;


import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.service.imp.IProjectPayment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectPaymentService implements IProjectPayment {
    //mã ngân hàng nhận tiền"MB,vietcombank"
    @Value("${app.sepay.bank-code}")
    private String sepayBankCode;

    //stk nhận tiền trong sepay
    @Value("${app.sepay.account-number}")
    private String sepayAccountNumber;

    @Value("${app.sepay.webhook-secret}")
    private String sepayWebhookSecret;

    private final PaymentTransactionRepo paymentTransactionRepo;
    private final PaymentTransactionMapper paymentTransactionMapper;
    private final CurrentUserService currentUserService;
    private final ClientProfileRepo clientProfileRepo;
    private final ProjectRepo projectRepo;
    private final ProjectEscrowRepo projectEscrowRepo;

    @Transactional
    @Override
    public ProjectPaymentResponse createProjectPayment(Long projectId) {

        Project project = findByProjectId(projectId);
        User currentUser = currentUserService.getCurrentUser();
        ClientProfile clientProfile =  findClientProfileByCurrentUser(currentUser);
        checkOwnerClient(project,clientProfile);
        checkProjectStatusWaitingEscrow(project);

        if(project.getPaymentDeadlineAt().isBefore(LocalDateTime.now())){
            checkPaymentDeadline(project);
        }
        ProjectEscrow projectEscrow = findProjectEscrowByProjectId(project.getProjectId());
        //kiểm tra có transaction chưa nếu có dùng lại , nếu chưa tạo cái mới
        PaymentTransaction paymentTransaction = paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                 PaymentReferenceType.PROJECT
                ,project.getProjectId()
                ,PaymentStatus.PENDING
                ,PaymentMethod.SEPAY).orElseGet(() -> createPendingProjectPayment(project,projectEscrow,currentUser));
        String qrUrl = buildSepayQrUrl(paymentTransaction.getAmount(),paymentTransaction.getPaymentCode());

        return paymentTransactionMapper.toProjectPaymentResponse(paymentTransaction,project.getProjectId(),qrUrl);
    }

    private PaymentTransaction createPendingProjectPayment(Project project , ProjectEscrow escrow, User currentUser) {
        PaymentTransaction paymentTransaction = PaymentTransaction
                .builder()
                .projectEscrowId(escrow.getProjectEscrowId())
                .expertServiceId(null)
                .senderId(currentUser.getUserId())
                .receiverId(null)
                .sourceWalletId(null)
                .targetWalletId(null)
                .amount(project.getAgreedPrice())
                .currency("VND")
                .transactionType(TransactionType.PROJECT_ESCROW)
                .paymentMethod(PaymentMethod.SEPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .referenceId(project.getProjectId())
                .paymentReferenceType(PaymentReferenceType.PROJECT)
                .providerName("SEPAY")
                .paymentCode(generatePaymentCode(project.getProjectId()))
                .providerTransactionCode(null)
                .providerContent(null)
                .paidAt(null)
                .expiredAt(project.getPaymentDeadlineAt())
                .build();
        return paymentTransactionRepo.save(paymentTransaction);
    }
    private String generatePaymentCode(Long projectId) {
        String paymentCode = UUID.randomUUID().toString().replace("-","").substring(0, 8).toUpperCase();
        return "AIT-PROJ-" + projectId + "-" + paymentCode;
    }
    private String buildSepayQrUrl(BigDecimal amount,String paymentCode) {
        String encodedDescription = URLEncoder.encode(paymentCode, StandardCharsets.UTF_8);
        String qrAmount = amount.setScale(0, java.math.RoundingMode.UNNECESSARY).toPlainString();
        return  "https://qr.sepay.vn/img"
                + "?bank=" + sepayBankCode
                + "&acc=" + sepayAccountNumber
                + "&amount=" + qrAmount
                + "&des=" + encodedDescription;
    }

    private void checkOwnerClient(Project project , ClientProfile clientProfile) {
        Long OwnerClientId = project.getInvitation().getExpertApplication().getJobpost().getClientProfile().getClientProfileId();

        if(!OwnerClientId.equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not owner client of this project");
        }
    }

    private Project findByProjectId(Long projectId) {
        return projectRepo.findByProjectId(projectId).orElseThrow(() -> new GlobalException(404, "Project not found"));
    }

    private ClientProfile findClientProfileByCurrentUser(User currentUser) {
        return clientProfileRepo.findByUser(currentUser).orElseThrow(() -> new GlobalException(403, "Only client can create project payment"));
    }

    //checkstatusproject = waitingescrow
    private void checkProjectStatusWaitingEscrow(Project project) {
        if(!ProjectStatus.WAITING_ESCROW.equals(project.getProjectStatus())) {
            throw new GlobalException(403, "Project status is not waiting escrow");
        }
    }
    // laaysn projectEscrow by id
    private ProjectEscrow findProjectEscrowByProjectId(Long projectId) {
        return projectEscrowRepo.findByProjectId(projectId).orElseThrow(() -> new GlobalException(404, "Project escrow not found"));
    }

   // quá hạn thì dùng hàm này để đổi trạng thái của payment
    private void expirePendingSepayPayment(Project project) {
        paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                PaymentReferenceType.PROJECT
                ,project.getProjectId()
                ,PaymentStatus.PENDING
                ,PaymentMethod.SEPAY).
                ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.EXPIRED);
            paymentTransactionRepo.save(payment);
        });
    }
    // quá hạn đổi trạng thái projectEscrow sang canceled
    private void cancelProjectEscrow(Project project) {
        projectEscrowRepo.findByProjectId(project.getProjectId())
                .ifPresent(projectEscrow -> {
            projectEscrow.setEscrowStatus(EscrowStatus.CANCELED);
            projectEscrowRepo.save(projectEscrow);
        });
    }

    // kiểm tra thanh toán của client có qua hạn chưa -> quá hạn thanh toán thì phải đổi trạng thái của Projecr và đổi luôn của payment và projectEscrow
    private void checkPaymentDeadline(Project project) {
            expirePendingSepayPayment(project);
            cancelProjectEscrow(project);
            project.setProjectStatus(ProjectStatus.CANCELED);
            projectRepo.save(project);
            throw new GlobalException(403, "Payment deadline is expired");


    }


}
