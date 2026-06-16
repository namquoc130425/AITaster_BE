package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.BlockedContentRequest;
import com.example.AiTaster.entity.BlockedContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Service;

@Mapper(componentModel = "spring")
public interface BlockedContentMapper {

    BlockedContent toEntity(BlockedContentRequest request);


    @Mapping(target = "blockedContentId", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    void updateEntity(BlockedContentRequest request, @MappingTarget BlockedContent entity);
}
