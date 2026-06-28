package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.NotificationCreateRequest;
import com.example.AiTaster.dto.response.NotificationResponse;
import com.example.AiTaster.entity.Notification;
import com.example.AiTaster.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "notificationId", ignore = true)
    @Mapping(target = "user", source = "receiver")
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "notificationType", source = "request.notificationType")
    @Mapping(target = "referenceType", source = "request.referenceType")
    @Mapping(target = "referenceId", source = "request.referenceId")
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    Notification toEntity(NotificationCreateRequest request, User receiver);

    @Mapping(target = "userId", source = "user.userId")
    NotificationResponse toResponse(Notification notification);
}