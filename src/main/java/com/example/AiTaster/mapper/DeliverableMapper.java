package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.DeliverableResponse;
import com.example.AiTaster.entity.Deliverable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = ServiceFileMapper.class)
public interface DeliverableMapper {
    @Mapping(target = "files", source = "serviceFile")
    DeliverableResponse toResponse(Deliverable deliverable);
}
