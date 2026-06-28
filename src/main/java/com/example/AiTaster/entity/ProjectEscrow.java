package com.example.AiTaster.entity;


import com.example.AiTaster.constant.EscrowStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectEscrow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long projectEscrowId;

    // Project nào đang được giữ tiền.
    @Column(nullable = false, unique = true)
    Long projectId;

    @Column(nullable = false)
    Long clientProfileId;

    @Column(nullable = false)
    Long expertProfileId;

    // Số tiền đã chốt từ Project/Invitation.
    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal agreedAmount;

    // Số tiền thật đang bị giữ. Ban đầu = 0, webhook success mới set.
    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal heldAmount;

    // Phí nền tảng. Phase đầu có thể để 0.
    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal platformFee;

    // Số tiền expert sẽ nhận sau khi trừ phí.
    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal expertAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    EscrowStatus escrowStatus;

    // thời điểm tiền bắt đầu được giữ thật.
    // Ban đầu null, webhook success mới set.
    LocalDateTime startedAt;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @PrePersist
    public void prePersist() {
        if (heldAmount == null) heldAmount = BigDecimal.ZERO;
        if (platformFee == null) platformFee = BigDecimal.ZERO;
        if (expertAmount == null && agreedAmount != null) {
            expertAmount = agreedAmount.subtract(platformFee);
        }
        if (escrowStatus == null) escrowStatus = EscrowStatus.WAITING_PAYMENT;
    }
}
