package com.example.AiTaster.service;

import com.example.AiTaster.constant.*;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component // Đăng ký class này vào Spring container.
@RequiredArgsConstructor
public class InvitationPaymentExpirationScheduler {
    private final InvitationRepo invitationRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;

    @Transactional // Một lần job chạy nằm trong một transaction.
    @Scheduled(fixedDelayString = "${app.jobs.invitation-expiration.fixed-delay-ms:60000}") // 60s sẽ chạy 1 lần
    public void expireInvitationAndPaymentDeadlines() {
        LocalDateTime now = LocalDateTime.now(); // Lấy giờ server hiện tại.

        expirePendingInvitations(now); // Dọn invitation PENDING quá hạn.
        expireAcceptedInvitationsWaitingForPayment(now); // Dọn invitation ACCEPTED quá hạn thanh toán.
    }

//trường hợp Invitation đã được gửi nhưng expert không phản hồi trong 24h thì đổi sang status EXPIRED
    private void expirePendingInvitations(LocalDateTime now) {
        List<Invitation> expiredInvitations =
                invitationRepo.findByInvitationStatusAndExpiresAtBefore(InvitationStatus.PENDING, now);

        if (expiredInvitations.isEmpty()) {
            return; // Không có gì quá hạn thì kết thúc.
        }

        expiredInvitations.forEach(invitation -> {
            invitation.setInvitationStatus(InvitationStatus.EXPIRED); // Đổi sang hết hạn phản hồi.
            invitation.setRespondedAt(null); // Không có thời điểm expert phản hồi.
        });

        invitationRepo.saveAll(expiredInvitations); // Lưu tất cả thay đổi vào DB.
        log.info("Expired {} pending invitations", expiredInvitations.size()); // Ghi log để dễ kiểm tra.
    }

    //trường hợp Invitation đã được expert chấp nhận nhưng chưa thanh toán trong 24h thì đổi sang status PAYMENT_EXPIRED
    private void expireAcceptedInvitationsWaitingForPayment(LocalDateTime now) {
        LocalDateTime deadline = now.minusHours(24); // Giả sử thời hạn thanh toán là 24 giờ kể từ khi invitation được chấp nhận.
        List<Invitation> invitations  =  invitationRepo.findAcceptedPaymentExpiredWithoutProject(InvitationStatus.ACCEPTED, deadline);

        if (invitations.isEmpty()) {
            return; // Không có invitation nào quá hạn thanh toán.
        }

        invitations.forEach(invitation ->
                invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED)
        );

        invitationRepo.saveAll(invitations); // Lưu status PAYMENT_EXPIRED.
        expirePendingSepayPayments(invitations); // Payment liên quan cũng phải chuyển EXPIRED.

        log.info("Expired payment deadline for {} invitations", invitations.size());

    }



    //tìm những transaction có lời mời hết hạn thì đổi sang status Expired
    private void expirePendingSepayPayments(List<Invitation>  invitations) {
        List<Long> invitationIds = invitations.stream().map(Invitation :: getInvitationId).toList();

        List<PaymentTransaction> payments = paymentTransactionRepo.findByPaymentReferenceTypeAndReferenceIdInAndTransactionTypeAndPaymentStatusAndPaymentMethod(
                PaymentReferenceType.INVITATION,
                invitationIds,
                TransactionType.PROJECT_ESCROW_DEPOSIT,
                PaymentStatus.PENDING,
                PaymentMethod.SEPAY);

        payments.forEach(payment ->
                payment.setPaymentStatus(PaymentStatus.EXPIRED)
        );

        paymentTransactionRepo.saveAll(payments); // Lưu payment hết hạn.
    }

}
