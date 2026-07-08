package com.example.AiTaster.repository;

import com.example.AiTaster.dto.response.ExpertApplicationResponse;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.JobPost;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpertApplicationRepo extends JpaRepository<ExpertApplication, Long> {




    Optional<ExpertApplication> findByApplicationId(Long applicationId);

    boolean existsByJobpostAndExpertProfile(JobPost jobpost, ExpertProfile expertProfile);

    long countByJobpost(JobPost jobpost);

    List<ExpertApplication> findByJobpost(JobPost jobpost);

    List<ExpertApplication> findByExpertProfile(ExpertProfile expertProfile);

    @EntityGraph(attributePaths = {
            "jobpost",
            "jobpost.clientProfile",
            "jobpost.clientProfile.user",
            "expertProfile",
            "expertProfile.user"
    })
    Optional<ExpertApplication> findWithDetailByApplicationId(Long applicationId);
}
