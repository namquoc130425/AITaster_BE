package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.request.ClientRegisterRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.entity.ClientProfile;
import org.mapstruct.*;


@Mapper(componentModel = "spring",uses = UserMapper.class)
public interface ClientProfileMapper {



    // Chuyển dữ liệu yêu cầu sang entity.

    ClientProfile registerToEntity(ClientRegisterRequest request);

    // Chuyển entity sang dữ liệu trả về.
    ClientProfileResponse toResponse(ClientProfile clientProfile);

    // Cập nhật entity hiện có.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ClientProfile updateEntity( ClientProfileRequest request, @MappingTarget ClientProfile clientProfile );
}
