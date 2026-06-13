package com.example.AiTaster.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;


import javax.naming.Name;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ProposalUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long proposalUnlockId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false, unique = true)
    ExpertProposal proposal;
//Nhiều ProposalUnlock thuộc về 1 ClientProfile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_profile_id", nullable = false)
    ClientProfile clientProfile;

    Long paymentTransactionId;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal amount;

    @Column(nullable = false)
    Boolean isUnlocked;

    LocalDateTime unlockedAt;

    @PrePersist
    public void prePersist() {
        if (amount == null) amount = BigDecimal.ZERO;
        if (isUnlocked == null) isUnlocked = false;
    }
}


