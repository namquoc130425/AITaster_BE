package com.example.AiTaster.service;
import com.example.AiTaster.entity.Invitation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InvitationTimePolicy {

    @Value("${app.invitation.payment-window-hours:24}")
    private long paymentWindowHours;

    @Value("${app.invitation.response-window-hours:24}")
    private long responseWindowHours;

    // hạn client thanh toán tính từ lúc expert accept (respondedAt)
    public LocalDateTime paymentDeadline(Invitation invitation) {
        if (invitation == null || invitation.getRespondedAt() == null) {
            return null;
        }
        return invitation.getRespondedAt().plusHours(paymentWindowHours);
    }
    // True nếu đã quá hạn thanh toán tính tới thời điểm truyền vào (now).
    public boolean isPaymentExpired(Invitation invitation, LocalDateTime now) {
        LocalDateTime deadline = paymentDeadline(invitation);
        return deadline != null && deadline.isBefore(now);
    }

    // Hạn expert phải phản hồi, tính từ thời điểm truyền vào (now) + 24h
    public LocalDateTime responseDeadline(LocalDateTime now) {
        return now.plusHours(responseWindowHours);
    }
}
