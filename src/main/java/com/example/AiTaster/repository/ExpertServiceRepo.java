package com.example.AiTaster.repository;

import com.example.AiTaster.constant.ServiceStatus;

import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ExpertServiceRepo extends JpaRepository<ExpertService, Long>, JpaSpecificationExecutor<ExpertService> {

    List<ExpertService> findByServiceStatus(ServiceStatus serviceStatus);

    List<ExpertService> findByExpertProfile(ExpertProfile expertProfile);

    List<ExpertService> findByExpertProfileAndServiceStatusNot(
            ExpertProfile expertProfile,
            ServiceStatus serviceStatus
    );
}
