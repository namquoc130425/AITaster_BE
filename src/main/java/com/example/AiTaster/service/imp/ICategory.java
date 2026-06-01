package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.CategoryRequest;
import com.example.AiTaster.dto.response.CategoryResponse;

import java.util.List;

public interface ICategory {
    List<CategoryResponse> getAll();

    CategoryResponse getByCategoryId(Long id);

    CategoryResponse CreateCategory(CategoryRequest category);

    CategoryResponse UpdateCategory(Long id ,CategoryRequest category);

   Void DeleteCategory(long id);


}
