package com.example.AiTaster.entity;

import com.example.AiTaster.constant.UserWalletStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long userWalletId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    User user;

    @Column(nullable = false)
    BigDecimal balance;

    @Column(nullable = false)
    BigDecimal frozenBalance;

    String currency;

    @Enumerated(EnumType.STRING)
    UserWalletStatus status;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;
}