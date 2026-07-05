package com.example.AiTaster.service;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.repository.InvitationRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationPaymentExpirationScheduler {
    private final InvitationRepo invitationRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;

    @Transactional
    @Scheduled(fixedDelayString = "${app.jobs.invitation-expiration.fixed-delay-ms:60000}")
    public void expireInvitationAndPaymentDeadlines() {
        LocalDateTime now = LocalDateTime.now();

        expirePendingInvitations(now);
        expireAcceptedInvitationsWaitingForPayment(now);
    }

    private void expirePendingInvitations(LocalDateTime now) {
        List<Invitation> expiredInvitations =
                invitationRepo.findByInvitationStatusAndExpiresAtBefore(InvitationStatus.PENDING, now);

        if (expiredInvitations.isEmpty()) {
            return;
        }

        expiredInvitations.forEach(invitation -> {
            invitation.setInvitationStatus(InvitationStatus.EXPIRED);
            invitation.setRespondedAt(null);
        });

        invitationRepo.saveAll(expiredInvitations);
        log.info("Expired {} pending invitations", expiredInvitations.size());
    }

    private void expireAcceptedInvitationsWaitingForPayment(LocalDateTime now) {
        LocalDateTime deadline = now.minusHours(24);
        List<Invitation> invitations =
                invitationRepo.findAcceptedPaymentExpiredWithoutProject(InvitationStatus.ACCEPTED, deadline);

        if (invitations.isEmpty()) {
            return;
        }

        invitations.forEach(invitation ->
                invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED)
        );

        invitationRepo.saveAll(invitations);
        expirePendingSepayPayments(invitations);

        log.info("Expired payment deadline for {} invitations", invitations.size());
    }

    private void expirePendingSepayPayments(List<Invitation> invitations) {
        List<Long> invitationIds = invitations.stream()
                .map(Invitation::getInvitationId)
                .toList();

        List<PaymentTransaction> payments =
                paymentTransactionRepo.findByPaymentReferenceTypeAndReferenceIdInAndTransactionTypeAndPaymentStatusAndPaymentMethod(
                        PaymentReferenceType.INVITATION,
                        invitationIds,
                        TransactionType.PROJECT_ESCROW_DEPOSIT,
                        PaymentStatus.PENDING,
                        PaymentMethod.SEPAY
                );

        payments.forEach(payment -> payment.setPaymentStatus(PaymentStatus.EXPIRED));
        paymentTransactionRepo.saveAll(payments);
    }
}
