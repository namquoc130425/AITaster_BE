package com.example.AiTaster.repository;

import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserWalletRepo extends JpaRepository<UserWallet, Long> {

    Optional<UserWallet> findByUser(User user);

    Optional<UserWallet> findByUserWalletId(Long UserWalletId);



}