package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.ServiceFileResponse;
import org.mapstruct.Mapper;
import com.example.AiTaster.entity.ServiceFile;

@Mapper(componentModel = "spring")
public interface ServiceFileMapper {

    ServiceFileResponse toResponse(ServiceFile serviceFile);
}
