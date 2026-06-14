package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.ExpertProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ExpertProfileMapper {
    //request -> entity ( expert register request chuyển thành expert Profile )

    //    @Mapping(target = "yearOfExperience", source = "yearsOfExperience") => học thêm đi
    ExpertProfile registertoEntity(ExpertRegisterRequest request);


    //entity -> responce(expert profile chuyển thành expert profile response)
    // @Mapping(target = "userId", source = "user.userId")
//    @Mapping(target = "yearsOfExperience", source = "yearOfExperience") => để thứ xàm lồn , đéo hiểu bản chất
    ExpertProfileResponse toResponse(ExpertProfile expertProfile);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//    @Mapping(target = "yearOfExperience", source = "yearsOfExperience") => code xàm lon
    void updateEntity(ExpertProfileRequest request, @MappingTarget ExpertProfile profile);


    // Update existing entity
}
