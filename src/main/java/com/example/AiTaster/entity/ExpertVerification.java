package com.example.AiTaster.entity;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long verificationId;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_profile_id", nullable = false, unique = true)
    ExpertProfile expertProfile;

    @Column(nullable = false, length = 1000)
    String certificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ExpertVerificationStatus verificationStatus;

    @Column(columnDefinition = "TEXT")
    String rejectReason;

    LocalDateTime verifiedAt;

    Long verifiedByAdminId;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (verificationStatus == null) {
            verificationStatus = ExpertVerificationStatus.SUBMITTED;
        }
    }
}
