package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.MessageResponse;
import com.example.AiTaster.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "conversationId", source = "conversation.conversationId")
    @Mapping(target = "senderId", source = "sender.userId")
    @Mapping(target = "senderName", source = "sender.fullName")
    @Mapping(target = "senderAvatarUrl", source = "sender.avatarUrl")
    @Mapping(target = "receiverId", source = "receiver.userId")
    @Mapping(target = "receiverName", source = "receiver.fullName")
    @Mapping(target = "receiverAvatarUrl", source = "receiver.avatarUrl")
    MessageResponse toResponse(Message message);
}
