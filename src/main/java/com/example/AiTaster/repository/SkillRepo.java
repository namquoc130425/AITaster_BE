package com.example.AiTaster.repository;


import com.example.AiTaster.entity.Skill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface SkillRepo extends JpaRepository<Skill,Long > {

    boolean existsBySkillName (String skillName);



    List<Skill> findSkillsBySkillId(long skillId);
}
