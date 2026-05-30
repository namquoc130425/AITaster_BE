package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.CategoryRequest;
import com.example.AiTaster.dto.response.CategoryResponse;
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.CategoryMappper;
import com.example.AiTaster.repository.CategoryRepo;
import com.example.AiTaster.service.imp.ICategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CategoryService implements ICategory {
@Autowired
CategoryRepo categoryRepo;
@Autowired
    CategoryMappper categoryMapper;

    @Override
    public List<CategoryResponse> getAll() {
        List<CategoryResponse> list = categoryRepo.findAll().stream().map(categoryMapper::toResponse).toList();

        return list;
    }

    @Override
    public CategoryResponse getByCategoryId(Long id) {
        Category category = categoryRepo.findById(id).orElseThrow(() -> new GlobalException("Category not found with id: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse CreateCategory(CategoryRequest category) {
        Category categoryEntity = categoryMapper.toEntity(category);
        categoryEntity = categoryRepo.save(categoryEntity);

        return categoryMapper.toResponse(categoryEntity);
    }

    @Override
    public CategoryResponse UpdateCategory(Long id, CategoryRequest category) {
        Category categoryid = categoryRepo.findById(id).orElseThrow(() -> new GlobalException("Category not found with id: " + id));
        Category entity = categoryMapper.updateEntity(category, categoryid);
         categoryRepo.save(entity);
         CategoryResponse categoryResponse = categoryMapper.toResponse(entity);
        return categoryResponse;
    }



    @Override
    public Void DeleteCategory(long id) {
        Category category = categoryRepo.findById(id).orElseThrow(() -> new GlobalException("Category not found with id: " + id));
        categoryRepo.delete(category);
        return null;
    }
}
