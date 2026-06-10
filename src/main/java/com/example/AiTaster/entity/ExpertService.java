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

    String serviceDescription;

    BigDecimal serviceFee;

    String serviceImage;
    String videoDemo;

    ServiceStatus serviceStatus;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

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
    List<Skill> skills;
}
