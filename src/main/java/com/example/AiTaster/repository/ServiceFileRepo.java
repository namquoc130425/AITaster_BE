package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ServiceFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceFileRepo
        extends JpaRepository<ServiceFile, Long> {
}
