package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.RegisterRequest;
import com.example.AiTaster.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequest request);

   // User toEntity(UserRequest request);  // admin create user

    // chuyển dữ liệu từ  entity qua response
    UserResponse toResponser(User user);

    //update
   // @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
   // User updateEntity (UserRequest request, @MappingTarget User user);
}
