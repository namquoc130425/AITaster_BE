package com.example.AiTaster.repository;

import com.example.AiTaster.constant.RatingTargetType;
import com.example.AiTaster.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepo extends JpaRepository<Rating, Long>, JpaSpecificationExecutor<Rating> {

    boolean existsByClientProfile_ClientProfileIdAndExpertService_ServiceId(
            Long clientProfileId,
            Long serviceId
    );

    boolean existsByClientProfile_ClientProfileIdAndProject_ProjectId(
            Long clientProfileId,
            Long projectId
    );

    Optional<Rating> findByClientProfile_ClientProfileIdAndExpertService_ServiceId(
            Long clientProfileId,
            Long serviceId
    );

    Optional<Rating> findByClientProfile_ClientProfileIdAndProject_ProjectId(
            Long clientProfileId,
            Long projectId
    );

    long countByExpertService_ServiceIdAndTargetType(
            Long serviceId,
            RatingTargetType targetType
    );

    long countByExpertProfile_ExpertProfileIdAndTargetType(
            Long expertProfileId,
            RatingTargetType targetType
    );

    @Query("""
            SELECT AVG(r.rating)
            FROM Rating r
            WHERE r.expertService.serviceId = :serviceId
              AND r.targetType = :targetType
            """)
    Double averageByExpertServiceIdAndTargetType(
            @Param("serviceId") Long serviceId,
            @Param("targetType") RatingTargetType targetType
    );

    @Query("""
            SELECT AVG(r.rating)
            FROM Rating r
            WHERE r.expertProfile.expertProfileId = :expertProfileId
              AND r.targetType = :targetType
            """)
    Double averageByExpertProfileIdAndTargetType(
            @Param("expertProfileId") Long expertProfileId,
            @Param("targetType") RatingTargetType targetType
    );
}
