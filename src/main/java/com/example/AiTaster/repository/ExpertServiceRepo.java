package com.example.AiTaster.repository;

import com.example.AiTaster.constant.ServiceStatus;
<<<<<<< HEAD

import com.example.AiTaster.dto.response.ExpertServiceResponse;
=======
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
<<<<<<< HEAD

public interface ExpertServiceRepo extends JpaRepository<ExpertService, Long>, JpaSpecificationExecutor<ExpertService> {

=======
import java.util.Optional;

public interface ExpertServiceRepo extends JpaRepository<ExpertService, Long>, JpaSpecificationExecutor<ExpertService> {

    Optional<ExpertService> findByServiceId(Long serviceId);

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
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
<<<<<<< HEAD
=======

    List<ExpertService> findByExpertProfile_ExpertProfileIdAndServiceStatusNot(
            Long expertProfileId,
            ServiceStatus serviceStatus
    );

    List<ExpertService> findByServiceStatusInOrderByUpdateAtAsc(
            List<ServiceStatus> serviceStatuses
    );
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
}
