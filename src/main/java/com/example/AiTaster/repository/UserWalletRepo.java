package com.example.AiTaster.repository;

import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserWalletRepo extends JpaRepository<UserWallet, Long> {

    Optional<UserWallet> findByUser(User user);

    Optional<UserWallet> findByUserWalletId(Long UserWalletId);
   // lock này để tránh client bấm approve 2 lần làm trả tiền expert 2 lần.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT w
        FROM UserWallet w
        WHERE w.user = :user
    """)
    Optional<UserWallet> findByUserForUpdate(@Param("user") User user);

}