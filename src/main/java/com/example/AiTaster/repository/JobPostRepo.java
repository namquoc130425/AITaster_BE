package com.example.AiTaster.repository;

import com.example.AiTaster.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostRepo extends JpaRepository<JobPost, Long> {
}
