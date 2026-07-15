package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.CategoryResponse;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.dto.response.SkillResponse;
import com.example.AiTaster.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring",
uses = {SkillMapper.class,CategoryMappper.class, ServiceFile.class})

public interface ExpertServiceMapper {

    @Mapping(target = "expertProfile",source = "expertProfile")
    @Mapping(target = "serviceName",source = "request.serviceName")
    @Mapping(target = "serviceDescription",source = "request.serviceDescription")
    @Mapping(target = "serviceFee",source = "request.serviceFee")
    @Mapping(target = "serviceImage",source = "request.serviceImage")
    @Mapping(target = "videoDemo",source = "request.videoDemo")
    @Mapping(target = "serviceStatus",constant = "DRAFT")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    ExpertService toEntity(ExpertServiceRequest request,ExpertProfile expertProfile);


    @Mapping(target = "serviceFileResponse",source = "serviceFile")
    @Mapping(target = "expertProfileId", source = "expertProfile.expertProfileId")
    @Mapping(target = "expertUserId", source = "expertProfile.user.userId")
    @Mapping(target = "expertName", source = "expertProfile.user.fullName")
    @Mapping(target = "expertEmail", source = "expertProfile.user.email")
    ExpertServiceResponse toResponse(ExpertService expertService);
    @Mapping(target = "expertProfile", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "serviceStatus", ignore = true)
    void toUpdateEntity(ExpertServiceRequest request,@MappingTarget ExpertService expertService);

    default Long mapExpertProfileToexpertProfileId( ExpertProfile expertProfile) {
        if(expertProfile == null) {
            return null;
        }
        return expertProfile.getExpertProfileId();

    }


}
