package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.NotificationResponse;
import com.example.AiTaster.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.userId")
    NotificationResponse toResponse(Notification notification);
}