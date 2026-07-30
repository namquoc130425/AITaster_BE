package com.example.AiTaster.entity;


import com.example.AiTaster.constant.DisputeStatus;
import com.example.AiTaster.constant.DisputeDecision;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long disputeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliverable_id")
    Deliverable deliverable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_against_id", nullable = false)
    User reportedAgainst;

    @Column(nullable = false, columnDefinition = "TEXT")
    String reason;

    @Column(columnDefinition = "TEXT")
    String evidence;

    @Column(columnDefinition = "TEXT")
    String response;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    DisputeStatus disputeStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    DisputeDecision disputeDecision;

    @Column(precision = 12, scale = 2)
    BigDecimal refundAmount;

    @Column(precision = 12, scale = 2)
    BigDecimal releaseAmount;

    @CreationTimestamp
    LocalDateTime createAt;

    LocalDateTime resolvedAt;

    @PrePersist
    void prePersist() {
        if (disputeStatus == null) disputeStatus = DisputeStatus.OPEN;
        if (refundAmount == null) refundAmount = BigDecimal.ZERO;
        if (releaseAmount == null) releaseAmount = BigDecimal.ZERO;
    }

}
