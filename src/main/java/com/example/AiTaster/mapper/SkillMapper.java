package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.skillsRequest.SkillRequest;
import com.example.AiTaster.dto.response.skillsResponse.SkillResponse;
import com.example.AiTaster.entity.Skill;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    Skill toEntity(SkillRequest skillRequest);

    SkillResponse toResponse(Skill skill);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Skill updateEnity (SkillRequest skillRequest, @MappingTarget Skill skill);

}
