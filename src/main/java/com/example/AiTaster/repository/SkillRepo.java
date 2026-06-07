package com.example.AiTaster.repository;


import com.example.AiTaster.entity.Skill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface SkillRepo extends JpaRepository<Skill,Long > {

    boolean existsBySkillName (String skillName);

//    @Query("""
//            select new com.example.AiTaster.dto.response.Ai.AiSkillResult(
//                s.skillId,
//                s.skillName
//            )
//            from Skill s
//            where lower(s.skillName) like lower(concat('%', :keyword, '%'))
//            order by s.skillName asc
//            """)
//List<AiSkillResult> findBySkillNameKeyword(@Param("keyword") String keywords, Pageable pageable);

    List<Skill> findSkillsBySkillId(long skillId);
}
