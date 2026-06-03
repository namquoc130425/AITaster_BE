package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.SkillRequest;
import com.example.AiTaster.dto.response.SkillResponse;
import com.example.AiTaster.entity.Skill;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    Skill toEntity(SkillRequest skillRequest);

    SkillResponse toResponse(Skill skill);

    List<SkillResponse> toReponseList(List<Skill> skillList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Skill toUpdate(SkillRequest skillRequest, @MappingTarget Skill skill);
}
