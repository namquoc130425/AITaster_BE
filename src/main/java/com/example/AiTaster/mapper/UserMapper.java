package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.*;
import com.example.AiTaster.dto.response.AdminResponse;
import com.example.AiTaster.entity.User;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequest request);

   // User toEntity(UserRequest request);  // admin create user

    // chuyển dữ liệu từ  entity qua response
    UserResponse toResponser(User user);
    AdminResponse toAdminResponse(User user);

    User clientRegisterToUser(ClientRegisterRequest request);


    User expertRegisterToUser(ExpertRegisterRequest request);


    User adminRequestToUser(AdminRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromAdminRequest(AdminRequest request, @MappingTarget User user);


    User adminRegisterToUser(AdminRegisterRequest request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User updateUserFromClientProfileRequest(ClientProfileRequest request, @MappingTarget User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User updateUserFromExpertProfileRequest(ExpertProfileRequest request, @MappingTarget User user);

}
