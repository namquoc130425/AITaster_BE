package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.ExpertProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring",uses = UserMapper.class)
public interface ExpertProfileMapper {
    //request -> entity ( expert register request chuyển thành expert Profile )

    ExpertProfile registertoEntity(ExpertRegisterRequest request);



    //entity -> responce(expert profile chuyển thành expert profile response)
    // @Mapping(target = "userId", source = "user.userId")
    ExpertProfileResponse toResponse(ExpertProfile expertProfile);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ExpertProfileRequest request, @MappingTarget ExpertProfile profile);


    // Update existing entity
}
