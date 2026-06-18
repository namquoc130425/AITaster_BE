package com.example.AiTaster.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expert_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long proposalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
     ExpertApplication expertApplication;


    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT")
    String technologies;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    String detailContent;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal priceToUnlock;

    @Column(nullable = false)
    Boolean isDeleted;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @PrePersist
    public void prePersist() {
        if (priceToUnlock == null) {
            priceToUnlock = BigDecimal.ZERO;
        }

        if (isDeleted == null) {
            isDeleted = false;
        }

    }
}
