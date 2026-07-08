package com.example.AiTaster.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "expert_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"jobpost_id", "expertProfile_Id"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class ExpertApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobpost_id", nullable = false)
    JobPost jobpost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertProfile_Id", nullable = false)
    ExpertProfile expertProfile;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal expectedPrice;

    @Column(nullable = false)
    String estimatedTimeline;

    @Column(columnDefinition = "TEXT")
    String shortMessage;

    @OneToOne(mappedBy = "expertApplication", fetch = FetchType.LAZY)
    ExpertProposal expertProposal;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @OneToMany(mappedBy = "expertApplication")
    List<Invitation> invitations;
}
