package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.ExpertVerificationResponse;
import com.example.AiTaster.entity.ExpertVerification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpertVerificationMapper {
    ExpertVerificationResponse toResponse(ExpertVerification verification);
}
