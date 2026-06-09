package com.example.AiTaster.repository;

import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.entity.ExpertService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertServiceRepo extends JpaRepository<ExpertService, Long> {

}
