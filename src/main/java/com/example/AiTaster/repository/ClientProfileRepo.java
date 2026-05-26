package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientProfileRepo extends JpaRepository<ClientProfile, Long> {
    Optional<ClientProfile> findByClientProfileId(Long clientId);
    boolean existsByClientProfileId(Long clientId);
    Optional<ClientProfile> findByUser_UserId(Long userId);
    boolean existsByUser_UserId(Long userId);
}
