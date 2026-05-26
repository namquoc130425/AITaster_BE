package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.request.ClientRegisterRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.entity.ClientProfile;
import org.mapstruct.*;


@Mapper(componentModel = "spring",uses = UserMapper.class)
public interface ClientProfileMapper {



    // Request -> Entity
    @Mapping(target = "clientProfileId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    ClientProfile registerToEntity(ClientRegisterRequest request);

    // Entity -> Response

    ClientProfileResponse toResponse(ClientProfile clientProfile);

    // Update existing entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(
            ClientProfileRequest request,
            @MappingTarget ClientProfile clientProfile
    );
}
