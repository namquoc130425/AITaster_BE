package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.ClientRegisterRequest;
import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.request.RegisterRequest;
import com.example.AiTaster.entity.User;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequest request);

   // User toEntity(UserRequest request);  // admin create user

    // chuyển dữ liệu từ  entity qua response
    UserResponse toResponser(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "userStatus", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    User clientRegisterToUser(ClientRegisterRequest request);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "userStatus", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    User expertRegisterToUser(ExpertRegisterRequest request);
    //update
   // @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
   // User updateEntity (UserRequest request, @MappingTarget User user);
}
