package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.ServiceFileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.AiTaster.entity.ServiceFile;

@Mapper(componentModel = "spring")
public interface ServiceFileMapper {

    @Mapping(target = "fileName", expression = "java(resolveFileName(serviceFile.getProductFile()))")
    ServiceFileResponse toResponse(ServiceFile serviceFile);

    default String resolveFileName(String productFile) {
        if (productFile == null || productFile.isBlank()) {
            return null;
        }

        String filename = productFile.substring(productFile.lastIndexOf('/') + 1);
        int separatorIndex = filename.indexOf('_');

        if (separatorIndex >= 0 && separatorIndex + 1 < filename.length()) {
            return filename.substring(separatorIndex + 1);
        }

        return filename;
    }
}
