package com.example.AiTaster.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long clientServiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_profile_id", nullable = false)
    ClientProfile clientProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    ExpertService expertService;

    Long invoiceId;
    Long paymentTransactionId;
    String serviceName;
    String serviceType;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "TEXT")
    String serviceFile;

    @Column(columnDefinition = "TEXT")
    String videoDemo;

    @Column(columnDefinition = "TEXT")
    String instructionFile;

    Integer version;
    LocalDateTime receivedAt;
    LocalDateTime expiredAt;

    @CreationTimestamp
    LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (version == null) version = 1;
        if (receivedAt == null) receivedAt = LocalDateTime.now();
    }
}
