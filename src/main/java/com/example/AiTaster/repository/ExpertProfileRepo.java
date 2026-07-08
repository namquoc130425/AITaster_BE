package com.example.AiTaster.repository;


import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface ExpertProfileRepo extends JpaRepository<ExpertProfile, Long> {
    boolean existsByUser_UserId(Long userId);

    Optional<ExpertProfile> findByUser_UserId(Long userId);

    Optional<ExpertProfile> findByExpertProfileId(Long expertProfileId);

    Optional<ExpertProfile> findByUser(User user);
}
