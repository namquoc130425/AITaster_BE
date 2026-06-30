package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.ExpertProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ExpertProfileMapper {

    ExpertProfile registertoEntity(ExpertRegisterRequest request);

    ExpertProfileResponse toResponse(ExpertProfile expertProfile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "yearOfExperience", source = "yearOfExperience")
    void updateEntity(
            ExpertProfileRequest request,
            @MappingTarget ExpertProfile profile
    );
}