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
<<<<<<< HEAD
    @Mapping(target = "expertId", source = "expert.userId")
    @Mapping(target = "expertName", source = "expert.fullName")
    ConversationResponse toResponse(Conversation conversation);
}
=======
    @Mapping(target = "clientAvatarUrl", source = "client.avatarUrl")
    @Mapping(target = "expertId", source = "expert.userId")
    @Mapping(target = "expertName", source = "expert.fullName")
    @Mapping(target = "expertAvatarUrl", source = "expert.avatarUrl")
    @Mapping(target = "unreadCount", ignore = true)
    ConversationResponse toResponse(Conversation conversation);
}
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
