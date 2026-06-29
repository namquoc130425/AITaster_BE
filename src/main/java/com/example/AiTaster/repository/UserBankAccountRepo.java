package com.example.AiTaster.repository;

import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBankAccountRepo extends JpaRepository<UserBankAccount, Long> {
    Optional<UserBankAccount> findFirstByUserAndIsDefaultTrueOrderByCreateAtDesc(User user);

    boolean existsByUserAndAccountNumber(User user, String accountNumber);
}
