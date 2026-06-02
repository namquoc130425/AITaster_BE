package com.example.AiTaster.repository;

import com.example.AiTaster.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepo extends JpaRepository<Skill,Long > {

    boolean existsBySkillName (String skillName);
}
