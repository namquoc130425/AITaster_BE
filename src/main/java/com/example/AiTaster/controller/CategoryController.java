package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.Category.CategoryFilterRequest;
import com.example.AiTaster.dto.request.CategoryRequest;
import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.CategoryResponse;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @PostMapping("/filter")
    public ResponseEntity<APIResponse<PageResponse<CategoryResponse>>> getAllCategoriesPage(
            @RequestBody @Valid CategoryFilterRequest request
    ) {
        PageResponse<CategoryResponse> response =
                categoryService.getAllCategoriesPage(request);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get categories with filter and pagination successfully",
                        response
                )
        );
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    // @Valid để kích hoạt validation
    public ResponseEntity<APIResponse<CategoryResponse>> createCategory(@RequestBody @Valid CategoryRequest request) {
        CategoryResponse response = categoryService.CreateCategory(request);

        return ResponseEntity.status(201).body(APIResponse.response(201,"Created",response));
    }


    @GetMapping
    public ResponseEntity<APIResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> responses = categoryService.getAll();
        return ResponseEntity.ok(APIResponse.response(201, "Get all Category successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<CategoryResponse>> getById( @PathVariable Long id) {

        CategoryResponse response =  categoryService.getByCategoryId(id);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Get Category by id successfully",response));

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<CategoryResponse>> update(@Valid @PathVariable Long id, @RequestBody CategoryRequest request
    ) {
        CategoryResponse response =  categoryService.UpdateCategory(id,request);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Update Category successfully",response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<CategoryResponse>> delete(@PathVariable Long id) {
        categoryService.DeleteCategory(id);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Deleted Category successfully",null));
    }
}
