package com.example.AiTaster.repository;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertVerification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpertVerificationRepo extends JpaRepository<ExpertVerification, Long> {
    Optional<ExpertVerification> findByExpertProfile(ExpertProfile expertProfile);

    boolean existsByExpertProfile_ExpertProfileIdAndVerificationStatus(
            Long expertProfileId,
            ExpertVerificationStatus verificationStatus
    );

    @EntityGraph(attributePaths = {
            "expertProfile",
            "expertProfile.user"
    })
    List<ExpertVerification> findByVerificationStatus(ExpertVerificationStatus verificationStatus);
}
