package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.CategoryRequest;
import com.example.AiTaster.dto.response.CategoryResponse;

import com.example.AiTaster.entity.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CategoryMappper {
    // Chuyển dữ liệu yêu cầu sang entity.
    Category  toEntity(CategoryRequest request);

    // Chuyển entity sang dữ liệu trả về.
    CategoryResponse toResponse(Category category);

    // Cập nhật entity hiện có.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Category updateEntity( CategoryRequest categoryRequest,@MappingTarget Category category);


}
