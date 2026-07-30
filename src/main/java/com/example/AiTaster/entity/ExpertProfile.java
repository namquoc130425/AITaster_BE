package com.example.AiTaster.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ExpertProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long expertProfileId;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    User user;

    @Column(columnDefinition = "TEXT")
    String bio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @ManyToMany
    @JoinTable(
            name = "expert_profile_skill",
            joinColumns = @JoinColumn(name = "expert_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    List<Skill> skills;

    Integer yearOfExperience;

    BigDecimal rating;

    Integer ratingCount;

    Integer completedProjects;

    String portfolioUrl;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @OneToOne(mappedBy = "expertProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    ExpertVerification verification;


    @PrePersist
    public void prePersist(){
        // nếu chưa có rating
        // expert mới đăng ký sẽ mặc định = 0.0
        if (rating == null) {
            rating = BigDecimal.ZERO;
        }

        if (ratingCount == null) {
            ratingCount = 0;
        }

        // nếu chưa có project hoàn thành
        // expert mới sẽ mặc định = 0
        if (completedProjects == null) {
            completedProjects = 0;
        }
    }

    @OneToMany(mappedBy = "expertProfile")
    List<ExpertService> expertServices;



    @OneToMany(mappedBy = "expertProfile")
    List<ExpertApplication> expertApplications;
}
