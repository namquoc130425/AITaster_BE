package com.example.AiTaster.entity;

import com.example.AiTaster.constant.ProductType;
import jakarta.persistence.*;     // <-- dùng cái này
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
public class ServiceFile {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long serviceFileId;

    @Column(columnDefinition = "TEXT")
    String fileContent;

    @Enumerated(EnumType.STRING)
    ProductType productType;

    @Column(columnDefinition = "TEXT")
    String productFile;

    Boolean isActive;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertServiceId")
    ExpertService expertService;

    //    @OneToOne(fetch = FetchType.LAZY)

//    @JoinColumn(name = "deliverableId")

//    Deliverable deliverable;
}