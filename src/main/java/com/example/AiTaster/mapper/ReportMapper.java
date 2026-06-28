package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ReportRequest;
import com.example.AiTaster.dto.response.ReportResponse;
import com.example.AiTaster.entity.Report;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "reportId", ignore = true)
    @Mapping(target = "reporter", ignore = true)
    @Mapping(target = "reportedUser", ignore = true)
    @Mapping(target = "evidenceFile", ignore = true)
    @Mapping(target = "reportStatus", ignore = true)
    @Mapping(target = "adminResponse", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Report toEntity(ReportRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "reportId", ignore = true)
    @Mapping(target = "reporter", ignore = true)
    @Mapping(target = "reportedUser", ignore = true)
    @Mapping(target = "evidenceFile", ignore = true)
    @Mapping(target = "reportStatus", ignore = true)
    @Mapping(target = "adminResponse", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(
            ReportRequest request,
            @MappingTarget Report report
    );

    @Mapping(target = "reporterId", source = "reporter.userId")
    @Mapping(target = "reporterName", source = "reporter.fullName")
    @Mapping(target = "reportedUserId", source = "reportedUser.userId")
    @Mapping(target = "reportedUserName", source = "reportedUser.fullName")
    ReportResponse toResponse(Report report);
}