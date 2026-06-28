package com.example.AiTaster.entity;

import com.example.AiTaster.constant.MilestoneStep;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Deliverable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long deliverableId;

    @Column(nullable = false)
    Long projectId;

    @Column(nullable = false)
    Long milestoneId;

    @Column(nullable = false)
    Long expertId; // = expertProfileId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    MilestoneStep step;

    @Column(nullable = false)
    Integer version;

    LocalDateTime submittedAt;

    LocalDateTime reviewedAt;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @OneToMany(mappedBy = "deliverable",fetch = FetchType.LAZY)
    List<ServiceFile> serviceFile;


}
