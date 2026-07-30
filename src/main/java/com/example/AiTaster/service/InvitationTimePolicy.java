package com.example.AiTaster.service;
import com.example.AiTaster.entity.Invitation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class InvitationTimePolicy {

    private final Duration paymentWindow;
    private final Duration responseWindow;

    public InvitationTimePolicy(
            @Value("${app.invitation.payment-window-hours:24h}") Duration paymentWindow,
            @Value("${app.invitation.response-window-hours:24h}") Duration responseWindow
    ) {
        this.paymentWindow = paymentWindow;
        this.responseWindow = responseWindow;
    }

    // hạn client thanh toán tính từ lúc expert accept (respondedAt)
    public LocalDateTime paymentDeadline(Invitation invitation) {
        if (invitation == null || invitation.getRespondedAt() == null) {
            return null;
        }
        return invitation.getRespondedAt().plus(paymentWindow);
    }
    // True nếu đã quá hạn thanh toán tính tới thời điểm truyền vào (now).
    public boolean isPaymentExpired(Invitation invitation, LocalDateTime now) {
        LocalDateTime deadline = paymentDeadline(invitation);
        return deadline != null && deadline.isBefore(now);
    }

    // Hạn expert phải phản hồi, tính từ thời điểm truyền vào theo cấu hình hiện tại.
    public LocalDateTime responseDeadline(LocalDateTime now) {
        return now.plus(responseWindow);
    }

    public LocalDateTime paymentCutoff(LocalDateTime now) {
        return now.minus(paymentWindow);
    }
}
