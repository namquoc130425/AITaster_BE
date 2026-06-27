package com.example.AiTaster.service;


import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.*;
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

    private final PaymentTransactionRepo paymentTransactionRepo;
    private final PaymentTransactionMapper paymentTransactionMapper;
    private final CurrentUserService currentUserService;
    private final ClientProfileRepo clientProfileRepo;
    private final InvitationRepo  invitationRepo;
    private final SepayGateway sepayGateway;

    @Transactional
    @Override
    public ProjectPaymentResponse createProjectPayment(Long invitationId) {


        User currentUser = currentUserService.getCurrentUser();
        ClientProfile clientProfile =  findClientProfileByCurrentUser(currentUser);
        Invitation invitation = findInvitation(invitationId);
        checkInvitationOwnerClient(invitation, clientProfile);
        ensureInvitationCanBePaid(invitation);



        //kiểm tra có transaction chưa nếu có dùng lại , nếu chưa tạo cái mới
        PaymentTransaction paymentTransaction = paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                 PaymentReferenceType.INVITATION
                ,invitation.getInvitationId()
                ,PaymentStatus.PENDING
                ,PaymentMethod.SEPAY).orElseGet(() -> createPendingProjectPayment(invitation, currentUser));
        SepayCheckoutFormResponse checkoutForm = sepayGateway.createCheckoutForm(paymentTransaction, invitation);
        // Trả về response đầy đủ cho FE render form hidden + submit.
        return paymentTransactionMapper.toInvitationPaymentResponse(
                paymentTransaction,
                invitation.getInvitationId(),
                checkoutForm);
    }

    private PaymentTransaction createPendingProjectPayment(Invitation invitation, User currentUser) {
        PaymentTransaction paymentTransaction = PaymentTransaction
                .builder()
                .projectEscrowId(null)
                .expertServiceId(null)
                .senderId(currentUser.getUserId())
                .receiverId(null)
                .sourceWalletId(null)
                .targetWalletId(null)
                .amount(invitation.getFinalOfferedPrice())
                .currency("VND")
                .transactionType(TransactionType.PROJECT_ESCROW)
                .paymentMethod(PaymentMethod.SEPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .referenceId(invitation.getInvitationId())
                .paymentReferenceType(PaymentReferenceType.INVITATION)
                .providerName("SEPAY")
                .paymentCode(generatePaymentCode(invitation.getInvitationId()))
                .providerTransactionCode(null)
                .providerContent(null)
                .paidAt(null)
                .expiredAt(invitation.getRespondedAt().plusHours(24))
                .build();
        return paymentTransactionRepo.save(paymentTransaction);
    }
    private String generatePaymentCode(Long invitationId) {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "AIT-INV-" + invitationId + "-" + randomPart;
    }
//    private String buildSepayQrUrl(BigDecimal amount,String paymentCode) {
//        String encodedDescription = URLEncoder.encode(paymentCode, StandardCharsets.UTF_8);
//        String qrAmount = amount.setScale(0, java.math.RoundingMode.UNNECESSARY).toPlainString();
//        return  "https://qr.sepay.vn/img"
//                + "?bank=" + sepayBankCode
//                + "&acc=" + sepayAccountNumber
//                + "&amount=" + qrAmount
//                + "&des=" + encodedDescription;
//    }

    // kiểm tra invi này có phải là của client không
    private void checkInvitationOwnerClient(Invitation invitation, ClientProfile clientProfile) {
        Long ownerClientId = invitation.getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getClientProfileId();

        if (!ownerClientId.equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not owner client of this invitation");
        }
    }


// tìm invitation theo id
    private Invitation findInvitation(Long invitationId) {
        return invitationRepo.findByInvitationId(invitationId)
                .orElseThrow(() -> new GlobalException(404, "Invitation not found"));
    }


    // kiểm tra trạng  thái cuả invitation có được Accpert chưa
    // accepted thì phải có respones
    // và lời mời phải còn hạn
    private void ensureInvitationCanBePaid(Invitation invitation) {
        if (!InvitationStatus.ACCEPTED.equals(invitation.getInvitationStatus())) {
            throw new GlobalException(400, "Invitation is not accepted");
        }

        if (invitation.getRespondedAt() == null) {
            throw new GlobalException(400, "Invitation response time is missing");
        }

        if (invitation.getRespondedAt().plusHours(24).isBefore(LocalDateTime.now())) {
            expireInvitationPayment(invitation);
            throw new GlobalException(403, "Payment deadline is expired");
        }
    }

    private void expireInvitationPayment(Invitation invitation) {
        invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED);
        invitationRepo.save(invitation);

        paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                PaymentReferenceType.INVITATION,
                invitation.getInvitationId(),
                PaymentStatus.PENDING,
                PaymentMethod.SEPAY
        ).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.EXPIRED);
            paymentTransactionRepo.save(payment);
        });
    }

    private ClientProfile findClientProfileByCurrentUser(User currentUser) {
        return clientProfileRepo.findByUser(currentUser).orElseThrow(() -> new GlobalException(403, "Only client can create project payment"));
    }

//    private Project findByProjectId(Long projectId) {
//        return projectRepo.findByProjectId(projectId).orElseThrow(() -> new GlobalException(404, "Project not found"));
//    }



//    //checkstatusproject = waitingescrow
//    private void checkProjectStatusWaitingEscrow(Project project) {
//        if(!ProjectStatus.WAITING_ESCROW.equals(project.getProjectStatus())) {
//            throw new GlobalException(403, "Project status is not waiting escrow");
//        }
//    }
    // laaysn projectEscrow by id
//    private ProjectEscrow findProjectEscrowByProjectId(Long projectId) {
//        return projectEscrowRepo.findByProjectId(projectId).orElseThrow(() -> new GlobalException(404, "Project escrow not found"));
//    }

   // quá hạn thì dùng hàm này để đổi trạng thái của payment
//    private void expirePendingSepayPayment(Project project) {
//        paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
//                PaymentReferenceType.PROJECT
//                ,project.getProjectId()
//                ,PaymentStatus.PENDING
//                ,PaymentMethod.SEPAY).
//                ifPresent(payment -> {
//            payment.setPaymentStatus(PaymentStatus.EXPIRED);
//            paymentTransactionRepo.save(payment);
//        });
//    }
//    // quá hạn đổi trạng thái projectEscrow sang canceled
//    private void cancelProjectEscrow(Project project) {
//        projectEscrowRepo.findByProjectId(project.getProjectId())
//                .ifPresent(projectEscrow -> {
//            projectEscrow.setEscrowStatus(EscrowStatus.CANCELED);
//            projectEscrowRepo.save(projectEscrow);
//        });
//    }

//    // kiểm tra thanh toán của client có qua hạn chưa -> quá hạn thanh toán thì phải đổi trạng thái của Projecr và đổi luôn của payment và projectEscrow
//    private void checkPaymentDeadline(Project project) {
//            expirePendingSepayPayment(project);
//            cancelProjectEscrow(project);
//            project.setProjectStatus(ProjectStatus.CANCELED);
//            projectRepo.save(project);
//            throw new GlobalException(403, "Payment deadline is expired");
//
//
//    }


}
