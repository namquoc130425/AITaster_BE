package com.example.AiTaster.service.scheduler;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.repository.InvitationRepo;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.service.InvitationTimePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component // Đăng ký class này vào Spring container.
@RequiredArgsConstructor
public class InvitationPaymentExpirationScheduler {
    private final InvitationRepo invitationRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final JobPostRepo jobPostRepo;
    private final InvitationTimePolicy invitationTimePolicy;

    @Transactional // Một lần job chạy nằm trong một transaction.
    @Scheduled(fixedDelayString = "${app.jobs.invitation-expiration.fixed-delay-ms:60000}") // 60s sẽ chạy 1 lần
    public void expireInvitationAndPaymentDeadlines() {
        LocalDateTime now = LocalDateTime.now(); // Lấy giờ server hiện tại.

        expirePendingInvitations(now); // Dọn invitation PENDING quá hạn.
        expireAcceptedInvitationsWaitingForPayment(now); // Dọn invitation ACCEPTED quá hạn thanh toán.
    }

// Invitation đã gửi nhưng expert không phản hồi đúng hạn thì đổi sang EXPIRED.
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

    // Invitation đã được expert chấp nhận nhưng client chưa thanh toán đúng hạn thì đổi sang PAYMENT_EXPIRED.
    private void expireAcceptedInvitationsWaitingForPayment(LocalDateTime now) {
        LocalDateTime deadline = invitationTimePolicy.paymentCutoff(now);
        List<Invitation> invitations  =  invitationRepo.findAcceptedPaymentExpiredWithoutProject(InvitationStatus.ACCEPTED, deadline);

        if (invitations.isEmpty()) {
            return; // Không có invitation nào quá hạn thanh toán.
        }

        invitations.forEach(invitation -> {
            invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED);
            reopenJobPostIfClosedByThisInvitation(invitation);
        });

        invitationRepo.saveAll(invitations); // Lưu status PAYMENT_EXPIRED.
        expirePendingSepayPayments(invitations); // Payment liên quan cũng phải chuyển EXPIRED.

        log.info("Expired payment deadline for {} invitations", invitations.size());

    }

    // Mở lại JobPost nếu nó đang bị CLOSED chính vì invitation này (tránh JobPost kẹt vĩnh viễn khi Client lỡ hạn thanh toán)
    private void reopenJobPostIfClosedByThisInvitation(Invitation invitation) {
        JobPost jobPost = invitation.getExpertApplication().getJobpost();
        boolean closedByThisInvitation = JobpostStatus.CLOSED.equals(jobPost.getJobPostStatus())
                && invitation.getInvitationId().equals(jobPost.getClosedByInvitationId());

        if (closedByThisInvitation) {
            jobPostRepo.reopenJobPost(jobPost.getJobPostId(), JobpostStatus.OPEN);
        }
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
