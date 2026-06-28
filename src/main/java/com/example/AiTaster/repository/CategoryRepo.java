package com.example.AiTaster.repository;


import com.example.AiTaster.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface CategoryRepo extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Optional<Category> getCategoriesByCategoryId(Long categoryId);
    //  Optional<Category> findByCategoryid(Long id);
    //  Optional<Category> exisByCategoryName(String categoryName);
};



