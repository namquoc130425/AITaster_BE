package com.example.AiTaster.entity;

import com.example.AiTaster.constant.MilestoneStatus;
import com.example.AiTaster.constant.MilestoneStep;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMilestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long milestoneId;

  @Column(nullable = false, unique = true)
   Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 30)
    MilestoneStep currentStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    MilestoneStatus status;

    LocalDateTime step1ApprovedAt;

    LocalDateTime step2ApprovedAt;

    LocalDateTime finalApprovedAt;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "project_Id")
//    Project project;



    @PrePersist
    public void prePersist() {
        if (currentStep == null) currentStep = MilestoneStep.DOCUMENT;
        if (status == null) status = MilestoneStatus.WAITING_EXPERT_SUBMIT;
    }
}
