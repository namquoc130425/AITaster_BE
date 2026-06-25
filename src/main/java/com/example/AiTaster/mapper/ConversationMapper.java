package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.ConversationResponse;
import com.example.AiTaster.entity.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "clientId", source = "client.userId")
    @Mapping(target = "clientName", source = "client.fullName")
    @Mapping(target = "expertId", source = "expert.userId")
    @Mapping(target = "expertName", source = "expert.fullName")
    ConversationResponse toResponse(Conversation conversation);
}