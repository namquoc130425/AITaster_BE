package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.SkillRequest;
import com.example.AiTaster.dto.response.SkillResponse;
import com.example.AiTaster.entity.Skill;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

public interface ISkill {

    SkillResponse create(SkillRequest skillRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SkillResponse update(Long id, SkillRequest skillRequest);

    SkillResponse getById(Long id);

    List<SkillResponse> getAll();

    SkillResponse delete(Long id);
}
