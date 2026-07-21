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
<<<<<<< HEAD
    @Mapping(target = "receiverId", source = "receiver.userId")
    @Mapping(target = "receiverName", source = "receiver.fullName")
    MessageResponse toResponse(Message message);
}
=======
    @Mapping(target = "senderAvatarUrl", source = "sender.avatarUrl")
    @Mapping(target = "receiverId", source = "receiver.userId")
    @Mapping(target = "receiverName", source = "receiver.fullName")
    @Mapping(target = "receiverAvatarUrl", source = "receiver.avatarUrl")
    MessageResponse toResponse(Message message);
}
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
