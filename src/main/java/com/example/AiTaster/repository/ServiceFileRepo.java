package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ServiceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceFileRepo extends JpaRepository<ServiceFile, Long> {
    List<ServiceFile> findByDeliverable_DeliverableId(Long deliverableId);
}
