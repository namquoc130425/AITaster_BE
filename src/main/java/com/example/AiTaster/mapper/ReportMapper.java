package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ReportRequest;
import com.example.AiTaster.dto.response.ReportResponse;
import com.example.AiTaster.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    Report toEntity(ReportRequest request);

    void updateEntity(
            ReportRequest request,
            @MappingTarget Report report
    );

    @Mapping(target = "reporterId",
            source = "reporter.userId")
    @Mapping(target = "reporterName",
            source = "reporter.fullName")
    @Mapping(target = "reportedUserId",
            source = "reportedUser.userId")
    @Mapping(target = "reportedUserName",
            source = "reportedUser.fullName")
    ReportResponse toResponse(Report report);
}