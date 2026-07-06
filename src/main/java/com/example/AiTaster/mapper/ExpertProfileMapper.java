package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.Skill;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ExpertVerificationMapper.class})
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

    default String map(Category category) {
        return category == null ? null : category.getCategoryName();
    }

    default String map(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return null;
        }

        return skills.stream()
                .map(Skill::getSkillName)
                .collect(Collectors.joining(", "));
    }
}
