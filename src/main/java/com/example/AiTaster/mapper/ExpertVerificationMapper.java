package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.ExpertVerificationResponse;
import com.example.AiTaster.entity.ExpertVerification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpertVerificationMapper {
    @Mapping(target = "expertProfileId", source = "expertProfile.expertProfileId")
    @Mapping(target = "expertUserId", source = "expertProfile.user.userId")
    @Mapping(target = "expertName", source = "expertProfile.user.fullName")
    @Mapping(target = "expertEmail", source = "expertProfile.user.email")
    ExpertVerificationResponse toResponse(ExpertVerification verification);
}
