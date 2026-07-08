package com.example.AiTaster.entity;

import com.example.AiTaster.constant.ServiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long serviceId;

    String serviceName;

    @Column(columnDefinition = "TEXT")
    String serviceDescription;

    BigDecimal serviceFee;

    String serviceImage;

    String videoDemo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "varchar(30)")
    ServiceStatus serviceStatus;

    @Column(columnDefinition = "TEXT")
    String rejectionReason;

    LocalDateTime submittedAt;

    LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    User reviewedBy;

    Integer reviewCount;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_Id", nullable = false)
    Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertProfile_Id", nullable = false)
    ExpertProfile expertProfile;

    @ManyToMany
    @JoinTable(
            name = "ExpertService_Skill",
            joinColumns = @JoinColumn(name = "service_Id"),
            inverseJoinColumns = @JoinColumn(name = "skill_Id")
    )
    List<Skill> skills;

    @OneToOne(
            mappedBy = "expertService",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    ServiceFile serviceFile;

    @PrePersist
    public void prePersist() {
        if (serviceStatus == null) {
            serviceStatus = ServiceStatus.DRAFT;
        }

        if (reviewCount == null) {
            reviewCount = 0;
        }
    }
}
