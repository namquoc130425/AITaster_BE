package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ClientService;
import com.example.AiTaster.entity.ExpertService;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientServiceRepo extends JpaRepository<ClientService, Long> {
    boolean existsByClientProfileAndExpertService(ClientProfile clientProfile, ExpertService expertService);

    @EntityGraph(attributePaths = {
            "expertService",
            "expertService.expertProfile",
            "expertService.expertProfile.user",
            "expertService.serviceFile",
            "expertService.category",
            "expertService.skills"
    })
    List<ClientService> findByClientProfileOrderByCreatedAtDesc(ClientProfile clientProfile);

    @EntityGraph(attributePaths = {
            "expertService",
            "expertService.expertProfile",
            "expertService.expertProfile.user",
            "expertService.serviceFile",
            "expertService.category",
            "expertService.skills"
    })
    Optional<ClientService> findByClientServiceIdAndClientProfile(Long clientServiceId, ClientProfile clientProfile);
}
