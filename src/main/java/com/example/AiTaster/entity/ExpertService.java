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
<<<<<<< HEAD
=======

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long serviceId;

    String serviceName;

<<<<<<< HEAD
=======
    @Column(columnDefinition = "TEXT")
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    String serviceDescription;

    BigDecimal serviceFee;

    String serviceImage;

    String videoDemo;

    @Enumerated(EnumType.STRING)
<<<<<<< HEAD
    ServiceStatus serviceStatus;

=======
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

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

<<<<<<< HEAD
    //lazy lấy sau , khi nào gọi tới quan hệ thì querry them
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_Id",nullable = false)
    Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertProfile_Id",nullable = false)
    ExpertProfile expertProfile;

    @ManyToMany()
            @JoinTable(
                    name = "ExpertService_Skill",
                    joinColumns = @JoinColumn(name = "service_Id"),
                    inverseJoinColumns = @JoinColumn(name = "skill_Id")

            )
=======
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
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    List<Skill> skills;

    @OneToOne(
            mappedBy = "expertService",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    ServiceFile serviceFile;
<<<<<<< HEAD
=======

    @PrePersist
    public void prePersist() {
        if (serviceStatus == null) {
            serviceStatus = ServiceStatus.DRAFT;
        }

        if (reviewCount == null) {
            reviewCount = 0;
        }
    }
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
}
