package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.entity.ClientProfile;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClientProfileMapper {

    ClientProfileMapper INSTANCE = Mappers.getMapper(ClientProfileMapper.class);

    // Request -> Entity
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "clientProfileId", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    ClientProfile toEntity(ClientProfileRequest request);

    // Entity -> Response
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "clientProfileId", source = "clientProfileId")
    ClientProfileResponse toResponse(ClientProfile clientProfile);

    // Update existing entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(
            ClientProfileRequest request,
            @MappingTarget ClientProfile clientProfile
    );
}
