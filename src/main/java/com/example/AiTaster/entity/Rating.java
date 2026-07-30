package com.example.AiTaster.entity;

import com.example.AiTaster.constant.RatingTargetType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "rating",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rating_client_service",
                        columnNames = {"client_profile_id", "expert_service_id"}
                ),
                @UniqueConstraint(
                        name = "uk_rating_client_project",
                        columnNames = {"client_profile_id", "project_id"}
                )
        },
        indexes = {
                @Index(name = "idx_rating_service", columnList = "expert_service_id"),
                @Index(name = "idx_rating_expert", columnList = "expert_profile_id"),
                @Index(name = "idx_rating_project", columnList = "project_id"),
                @Index(name = "idx_rating_target", columnList = "target_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ratingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_profile_id", nullable = false)
    ClientProfile clientProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_profile_id", nullable = false)
    ExpertProfile expertProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_service_id")
    ExpertService expertService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    RatingTargetType targetType;

    @Column(nullable = false)
    Integer rating;

    @Column(columnDefinition = "TEXT")
    String review;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;
}
