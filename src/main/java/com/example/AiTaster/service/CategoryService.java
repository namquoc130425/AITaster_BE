package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.CategoryRequest;
import com.example.AiTaster.dto.response.CategoryResponse;
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.CategoryMappper;
import com.example.AiTaster.repository.CategoryRepo;
import com.example.AiTaster.service.imp.ICategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

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
        Category category = categoryRepo.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Category: " + ErrorCode.NOT_FOUND.getMessage()));
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
        Category categoryid = categoryRepo.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Category: " + ErrorCode.NOT_FOUND.getMessage()));
        Category entity = categoryMapper.updateEntity(category, categoryid);
         categoryRepo.save(entity);
         CategoryResponse categoryResponse = categoryMapper.toResponse(entity);
        return categoryResponse;
    }



    @Override
    public Void DeleteCategory(long id) {
        Category category = categoryRepo.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Category: " + ErrorCode.NOT_FOUND.getMessage()));
        categoryRepo.delete(category);
        return null;
    }


    // gọi ra nha chung , gọi trong create hoặc update á ( bạn làm lười lắm )
    private String generateSlug(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)   // tách dấu tiếng việt
                .replaceAll("\\p{M}", "")        // xóa toàn bộ dấu
                .toLowerCase(Locale.ROOT)                         // viết thường
                        .trim()                                   // xóa khoảng trắng đầu cuối
                        .replaceAll("[^a-z0-9\\s-]", "") // xóa kí tự đặc biệt
                        .replaceAll("\\s+", "-")   // đổi khoảng trắng thành dấu -
                        .replaceAll("-+", "-"); // nếu nhiều cách thì chỉ 1 dấu - tránh trường hợp nam---dep---trai
    }
}
