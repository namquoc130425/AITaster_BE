package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.skillsRequest.SkillRequest;
import com.example.AiTaster.dto.response.skillsResponse.SkillResponse;
import com.example.AiTaster.entity.Skill;
import jakarta.validation.Valid;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

public interface ISkill {

    SkillResponse create(@Valid SkillRequest  skillRequest);

    List<SkillResponse> getAll();

    SkillResponse getSkillById(long id);



     SkillResponse updateEnity(SkillRequest skillRequest, long id);

    SkillResponse deleteSkillById(long id);

}
