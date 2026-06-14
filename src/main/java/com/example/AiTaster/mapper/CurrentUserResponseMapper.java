package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.CurrentUserResponse;
import com.example.AiTaster.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ClientProfileMapper.class,ExpertProfileMapper.class} )
public interface CurrentUserResponseMapper {

    CurrentUserResponse toResponse(User user);

}
