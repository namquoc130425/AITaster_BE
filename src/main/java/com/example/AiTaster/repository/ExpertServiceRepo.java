package com.example.AiTaster.repository;

import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ExpertServiceRepo extends JpaRepository<ExpertService, Long>, JpaSpecificationExecutor<ExpertService> {

    Optional<ExpertService> findByServiceId(Long serviceId);

    List<ExpertService> findByServiceStatus(ServiceStatus serviceStatus);

    long countByExpertProfile_ExpertProfileIdAndServiceStatus(
            Long expertProfileId,
            ServiceStatus serviceStatus
    );

    List<ExpertService> findByExpertProfile(ExpertProfile expertProfile);

    List<ExpertService> findByExpertProfileAndServiceStatusNot(
            ExpertProfile expertProfile,
            ServiceStatus serviceStatus
    );

    List<ExpertService> findByExpertProfile_ExpertProfileIdAndServiceStatusNot(
            Long expertProfileId,
            ServiceStatus serviceStatus
    );

    List<ExpertService> findByServiceStatusInOrderByUpdateAtAsc(
            List<ServiceStatus> serviceStatuses
    );
}
