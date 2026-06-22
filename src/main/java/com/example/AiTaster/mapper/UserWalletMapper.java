package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.UserWalletResponse;
import com.example.AiTaster.entity.UserWallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserWalletMapper {

    @Mapping(target = "userId", source = "user.userId")
    UserWalletResponse toResponse(UserWallet wallet);

}