package com.example.AiTaster.repository;


import com.example.AiTaster.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CategoryRepo extends JpaRepository<Category, Long> {
  //  Optional<Category> findByCategoryid(Long id);
  //  Optional<Category> exisByCategoryName(String categoryName);

}
