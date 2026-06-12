package com.example.AiTaster.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "expert_proposals",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_jobpost_expert_proposal",
                        columnNames = {"jobpost_id", "expert_profile_id"}
                )
        }
)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobpost_id", nullable = false)
    JobPost jobpost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertProfile_Id")
    ExpertProfile expertProfile;


    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    String summary;

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
