package com.example.AiTaster.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long clientProfileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    String companyName;
    String contactName;

    @Column(columnDefinition = "TEXT")
    String description;

    String businessField;
    String address;

    LocalDateTime createAt;
    LocalDateTime updateAt;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        updateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = LocalDateTime.now();
    }
}
