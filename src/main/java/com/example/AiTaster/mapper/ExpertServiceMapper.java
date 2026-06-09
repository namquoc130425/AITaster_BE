package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface ExpertServiceMapper {

    @Mapping(target = "expertProfile",source = "expertProfile")
    @Mapping(target = "serviceName",source = "request.serviceName")
    @Mapping(target = "serviceDescription",source = "request.serviceDescription")
    @Mapping(target = "serviceFee",source = "request.serviceFee")
    @Mapping(target = "serviceImage",source = "request.serviceImage")
    @Mapping(target = "videoDemo",source = "request.videoDemo")
    @Mapping(target = "serviceStatus",constant = "OPEN")

    ExpertService toEntity(ExpertServiceRequest request);

    @Mapping(target = "expertProfile",source = "expertProfile")
    @Mapping(target = "serviceName",source = "expertService.serviceName")
    @Mapping(target = "serviceDescription",source = "expertService.serviceDescription")
    @Mapping(target = "serviceFee",source = "request.serviceFee")
    @Mapping(target = "serviceImage",source = "request.serviceImage")
    @Mapping(target = "videoDemo",source = "expertService.videoDemo")
    @Mapping(target = "serviceStatus",constant = "OPEN")
    ExpertServiceResponse toResponse(ExpertService expertService);

    void toUpdateEntity(ExpertServiceRequest request,@MappingTarget ExpertService expertService);

    default Long mapExpertProfileToexpertProfileId( ExpertProfile expertProfile) {
        if(expertProfile == null) {
            return null;
        }
        return expertProfile.getExpertProfileId();

    }
}
