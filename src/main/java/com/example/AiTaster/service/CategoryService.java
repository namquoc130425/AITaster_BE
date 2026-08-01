package com.example.AiTaster.service;

import com.example.AiTaster.Util.PageUtil;
import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.Category.CategoryFilterRequest;
import com.example.AiTaster.dto.request.CategoryRequest;
import com.example.AiTaster.dto.response.CategoryResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.CategoryMappper;
import com.example.AiTaster.repository.CategoryRepo;
import com.example.AiTaster.service.imp.ICategory;
import com.example.AiTaster.specification.CategorySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        Category category = categoryRepo.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(), "Danh mục: " + ErrorCode.NOT_FOUND.getMessage()));
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse CreateCategory(CategoryRequest request) {
        String categoryName = request.getCategoryName().trim();
        String slug = resolveSlug(request.getSlug(), categoryName);
        validateUniqueCategory(categoryName, slug, null);

        Category categoryEntity = categoryMapper.toEntity(request);
        applyNormalizedValues(categoryEntity, categoryName, slug, request.getDescription());
        categoryEntity = categoryRepo.save(categoryEntity);

        return categoryMapper.toResponse(categoryEntity);
    }

    @Override
    public CategoryResponse UpdateCategory(Long id, CategoryRequest request) {
        Category categoryid = categoryRepo.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(), "Danh mục: " + ErrorCode.NOT_FOUND.getMessage()));
        String categoryName = request.getCategoryName().trim();
        String slug = resolveSlug(request.getSlug(), categoryName);
        validateUniqueCategory(categoryName, slug, id);

        Category entity = categoryMapper.updateEntity(request, categoryid);
        applyNormalizedValues(entity, categoryName, slug, request.getDescription());
         categoryRepo.save(entity);
         CategoryResponse categoryResponse = categoryMapper.toResponse(entity);
        return categoryResponse;
    }



    @Override
    public Void DeleteCategory(long id) {
        Category category = categoryRepo.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(), "Danh mục: " + ErrorCode.NOT_FOUND.getMessage()));
        categoryRepo.delete(category);
        return null;
    }

    public PageResponse<CategoryResponse> getAllCategoriesPage(CategoryFilterRequest request) {
        Pageable pageable = PageUtil.createPageable(request);

        Page<Category> categoryPage =
                categoryRepo.findAll(CategorySpecification.filter(request), pageable);

        Page<CategoryResponse> responsePage =
                categoryPage.map(categoryMapper::toResponse);

        return PageResponse.fromPage(responsePage);
    }


    private String resolveSlug(String requestedSlug, String categoryName) {
        String source = requestedSlug == null || requestedSlug.isBlank()
                ? categoryName
                : requestedSlug;
        String slug = generateSlug(source);

        if (slug.isBlank() || slug.length() > 120) {
            throw new GlobalException(400, "Slug danh mục không hợp lệ");
        }

        return slug;
    }

    private void validateUniqueCategory(String categoryName, String slug, Long excludedId) {
        boolean duplicatedName = excludedId == null
                ? categoryRepo.existsByCategoryNameIgnoreCase(categoryName)
                : categoryRepo.existsByCategoryNameIgnoreCaseAndCategoryIdNot(categoryName, excludedId);
        boolean duplicatedSlug = excludedId == null
                ? categoryRepo.existsBySlugIgnoreCase(slug)
                : categoryRepo.existsBySlugIgnoreCaseAndCategoryIdNot(slug, excludedId);

        if (duplicatedName) {
            throw new GlobalException(409, "Tên danh mục đã tồn tại");
        }

        if (duplicatedSlug) {
            throw new GlobalException(409, "Slug danh mục đã tồn tại");
        }
    }

    private void applyNormalizedValues(
            Category category,
            String categoryName,
            String slug,
            String description
    ) {
        category.setCategoryName(categoryName);
        category.setSlug(slug);
        category.setDescription(description.trim());
    }

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
