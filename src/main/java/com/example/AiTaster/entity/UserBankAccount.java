package com.example.AiTaster.entity;

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
public class UserBankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long userBankAccountId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    User user;

    @Column(nullable = false, length = 30)
    String bankCode;

    @Column(nullable = false, length = 40)
    String accountNumber;

    @Column(nullable = false, length = 120)
    String accountHolderName;

    @Column(nullable = false)
    Boolean verified;

    @Column(nullable = false)
    Boolean isDefault;

    @Column(length = 10)
    String otpCode;

    LocalDateTime otpExpiredAt;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @PrePersist
    public void prePersist() {
        if (verified == null) verified = false;
        if (isDefault == null) isDefault = true;
    }
}
