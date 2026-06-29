package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.ConversationResponse;
import com.example.AiTaster.entity.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "applicationId", source = "expertApplication.applicationId")
    @Mapping(target = "jobPostId", source = "expertApplication.jobpost.jobPostId")
    @Mapping(target = "clientId", source = "client.userId")
    @Mapping(target = "clientName", source = "client.fullName")
    @Mapping(target = "expertId", source = "expert.userId")
    @Mapping(target = "expertName", source = "expert.fullName")
    @Mapping(target = "unreadCount", ignore = true)
    ConversationResponse toResponse(Conversation conversation);
}
