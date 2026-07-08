package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.ExpertProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ExpertVerificationMapper.class, CategoryMappper.class, SkillMapper.class})
public interface ExpertProfileMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "skills", ignore = true)
    ExpertProfile registertoEntity(ExpertRegisterRequest request);

    ExpertProfileResponse toResponse(ExpertProfile expertProfile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "yearOfExperience", source = "yearOfExperience")
    void updateEntity(
            ExpertProfileRequest request,
            @MappingTarget ExpertProfile profile
    );

}
