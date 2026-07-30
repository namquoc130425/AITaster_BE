package com.example.AiTaster.service;

import com.example.AiTaster.entity.Invitation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InvitationTimePolicyTest {

    private InvitationTimePolicy invitationTimePolicy;

    @BeforeEach
    void setUp() {
        invitationTimePolicy = new InvitationTimePolicy(
                Duration.ofHours(24),
                Duration.ofHours(24)
        );
    }

    @Test
    void paymentDeadline_returnsRespondedAtPlusPaymentWindow() {
        LocalDateTime respondedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        Invitation invitation = Invitation.builder().respondedAt(respondedAt).build();

        LocalDateTime deadline = invitationTimePolicy.paymentDeadline(invitation);

        assertThat(deadline).isEqualTo(respondedAt.plusHours(24));
    }

    @Test
    void paymentDeadline_returnsNull_whenRespondedAtIsNull() {
        Invitation invitation = Invitation.builder().respondedAt(null).build();

        assertThat(invitationTimePolicy.paymentDeadline(invitation)).isNull();
    }

    @Test
    void paymentDeadline_returnsNull_whenInvitationIsNull() {
        assertThat(invitationTimePolicy.paymentDeadline(null)).isNull();
    }

    @Test
    void isPaymentExpired_returnsTrue_whenNowIsAfterDeadline() {
        LocalDateTime respondedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        Invitation invitation = Invitation.builder().respondedAt(respondedAt).build();

        LocalDateTime now = respondedAt.plusHours(24).plusMinutes(1); // vừa quá hạn 1 phút

        assertThat(invitationTimePolicy.isPaymentExpired(invitation, now)).isTrue();
    }

    @Test
    void isPaymentExpired_returnsFalse_whenNowIsBeforeDeadline() {
        LocalDateTime respondedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        Invitation invitation = Invitation.builder().respondedAt(respondedAt).build();

        LocalDateTime now = respondedAt.plusHours(23); // còn 1 tiếng mới hết hạn

        assertThat(invitationTimePolicy.isPaymentExpired(invitation, now)).isFalse();
    }

    @Test
    void isPaymentExpired_returnsFalse_whenRespondedAtIsNull() {
        Invitation invitation = Invitation.builder().respondedAt(null).build();

        assertThat(invitationTimePolicy.isPaymentExpired(invitation, LocalDateTime.now())).isFalse();
    }

    @Test
    void responseDeadline_returnsNowPlusResponseWindow() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 8, 0);

        LocalDateTime deadline = invitationTimePolicy.responseDeadline(now);

        assertThat(deadline).isEqualTo(now.plusHours(24));
    }

    @Test
    void differentConfiguredWindow_isRespected() {
        invitationTimePolicy = new InvitationTimePolicy(
                Duration.ofMinutes(2),
                Duration.ofMinutes(2)
        );
        LocalDateTime respondedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        Invitation invitation = Invitation.builder().respondedAt(respondedAt).build();

        assertThat(invitationTimePolicy.paymentDeadline(invitation)).isEqualTo(respondedAt.plusMinutes(2));
        assertThat(invitationTimePolicy.isPaymentExpired(invitation, respondedAt.plusMinutes(2).plusSeconds(1))).isTrue();
    }
}
