package com.example.AiTaster.repository;

import com.example.AiTaster.dto.request.skillsRequest.SkillRequest;
import com.example.AiTaster.entity.Skill;
import jakarta.validation.constraints.NotBlank;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepo extends JpaRepository<Skill, Long> {

    boolean existsBySkillName(String skillName);


}
