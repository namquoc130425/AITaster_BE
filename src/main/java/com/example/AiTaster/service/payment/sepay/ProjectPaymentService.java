package com.example.AiTaster.service.payment.sepay;


import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.CurrentUserService;
import com.example.AiTaster.service.PendingPaymentService;
import com.example.AiTaster.service.SepayGateway;
import com.example.AiTaster.service.imp.IProjectPayment;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ProjectPaymentService implements IProjectPayment {

    private final PaymentTransactionRepo paymentTransactionRepo ;
    private final PaymentTransactionMapper paymentTransactionMapper;
    private final CurrentUserService currentUserService;
    private final ClientProfileRepo clientProfileRepo;
    private final InvitationRepo invitationRepo;
    private final SepayGateway sepayGateway;
    private final PendingPaymentService pendingPaymentService;

    @Transactional
    @Override
    public ProjectPaymentResponse createProjectPayment(Long invitationId) {


        User currentUser = currentUserService.getCurrentUser();
        ClientProfile clientProfile = findClientProfileByCurrentUser(currentUser);
        Invitation invitation = findInvitation(invitationId);
        checkInvitationOwnerClient(invitation, clientProfile);
        ensureInvitationCanBePaid(invitation);


        //kiểm tra có transaction chưa nếu có dùng lại , nếu chưa tạo cái mới
        PaymentTransaction paymentTransaction = pendingPaymentService.createPendingPaymentTransaction(
                currentUser.getUserId(),
                null,
                null,
                null,
                null,
                null,
                TransactionType.PROJECT_ESCROW_DEPOSIT,
                invitationId,
                PaymentReferenceType.INVITATION,
                invitation.getFinalOfferedPrice(),
                "SePay project escrow payment - invitation " + invitation.getInvitationId(),
                invitation.getRespondedAt().plusHours(24)

        );
        SepayCheckoutFormResponse checkoutForm = sepayGateway.createCheckoutForm(paymentTransaction);
        // Trả về response đầy đủ cho FE render form hidden + submit.
        return paymentTransactionMapper.toInvitationPaymentResponse(
                paymentTransaction,
                invitation.getInvitationId(),
                checkoutForm);
    }



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


}
